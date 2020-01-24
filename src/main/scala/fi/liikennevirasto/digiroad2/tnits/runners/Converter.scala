package fi.liikennevirasto.digiroad2.tnits.runners

import java.io.PrintWriter
import java.net.URLEncoder
import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

import fi.liikennevirasto.digiroad2.tnits.aineistot.{RemoteDataSetBusStop, RemoteDataset, RemoteNonStdDataset}
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.geojson.Feature
import fi.liikennevirasto.digiroad2.tnits.oth._
import fi.liikennevirasto.digiroad2.tnits.rosatte._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import java.io.OutputStream

case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String, client: Client, service: AssetRosatteConverter, source: String = "Regulation")

/** Base class to run conversion batch job. */
trait Converter {
  implicit def dateTimeOrdering: Ordering[Instant] = Ordering.fromLessThan(_ isBefore _ )
  def assetTypes: Seq[AssetType]
  def directory: String
  def getLatestEndTime: Option[Instant]
  def getOutputStreamSFTP(filename : String): OutputStream

  //For each page, OTH return 4000 record, if you want increase it please request to OTH Team
  private val LIMIT_RECORD_NUMBER = 4000

  case class OTHException(cause: Throwable) extends RuntimeException(cause)

  def recursiveCall(start: Instant, end: Instant, assetTypes: Seq[AssetType], pageNumber: Option[Int] = Some(1),
                    result: Seq[(AssetType, Seq[Feature[AssetProperties]])] = Seq()): Seq[(AssetType, Seq[Feature[AssetProperties]])] = {

    val assets = fetchAllChanges(start, end, assetTypes, pageNumber)
    val zippedAsset: Seq[(AssetType, Seq[Feature[AssetProperties]])] = assetTypes.zip(assets)

    val oversizeAssetTypes = zippedAsset.foldLeft(Seq.empty[AssetType]) { case (result, asset) =>
      if (asset._2.size == LIMIT_RECORD_NUMBER) result :+ asset._1 else result
    }

    if(oversizeAssetTypes.nonEmpty)
      recursiveCall(start, end, assetTypes, Some(pageNumber.get + 1), result)
    else {
      result
    }
  }

  def convert(logger: PrintWriter, fromDate: Option[Instant] = None, toDate: Option[Instant] = None ): Unit = {
    val (start, end) =  (fromDate, toDate) match {
      case (Some(startDate), Some(endDate)) => (startDate, endDate)
      case _ => (getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS)),
        Instant.now.minus(1, ChronoUnit.MINUTES))
    }

    logger.println(s"Start: $start")
    logger.println(s"End: $end")

    val assets = recursiveCall(start, end, assetTypes).groupBy(_._1).flatten(_._2).toSeq

    logger.println("fetched all changes, generating dataset")

    val dataSetId = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
    val filename = s"${URLEncoder.encode(dataSetId, "UTF-8")}.xml"

    try {
      // Create new stream to the SFTP server for replace a stream to the FTP server in the Future
      val outputStreamSFTP = getOutputStreamSFTP(filename)

      try {
        convertDataSet(assets, start, end, dataSetId, outputStreamSFTP)
      } finally {
        outputStreamSFTP.close()
      }
    } catch {
      case e: Throwable => logger.println("SFTP OutputStream  Failed with the follow message: ", e.getMessage)
    }

    logger.println(s"Dataset ID: $dataSetId")
    logger.println(s"dataset: $filename")
    logger.println("done!\n")
  }

  protected def fetchAllChanges(start: Instant, end: Instant, assetTypes: Seq[AssetType], pageNumber: Option[Int] = None): Seq[Seq[Feature[AssetProperties]]] = {
    try {
      val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
      val responses = Future.sequence(assetTypes.map { asset =>
        asset.client.fetchChanges(asset.apiEndPoint, start, end, pageNumber, executor)
      })
      Await.result(responses, 120.minutes)
    } catch {
      case err: Throwable =>
        throw OTHException(err)
    }
  }

  protected def convertDataSet(assets: Seq[(AssetType, Seq[Feature[AssetProperties]])], start: Instant, end: Instant, datasetID: String, outputStream: OutputStream): Unit = {
    RosatteConverter.convertDataSet(assets, start, end, datasetID, outputStream)
  }
}


class BusStopConverter extends Converter {
  override val assetTypes = Seq(
    AssetType("mass_transit_stops", "MassTransitStop", "MassTransitStop", "", BusStopOTHClient, new PointRosatteConverter)
  )
  override def getLatestEndTime: Option[Instant] = RemoteDataSetBusStop.getLatestEndTime
  override def directory: String = config.baseDirBusStopsSFTP
  override def getOutputStreamSFTP(filename : String): OutputStream = RemoteDataSetBusStop.getOutputStreamSFTP(filename, directory)


  override protected def convertDataSet(assets: Seq[(AssetType, Seq[Feature[AssetProperties]])], start: Instant, end: Instant, datasetID: String, outputStream: OutputStream): Unit = {
    BusStopValluConverter.convertDataSet(assets, outputStream)
  }

  /** Runs a conversion programmatically. */
  override def convert(logger: PrintWriter, fromDate: Option[Instant] = None, toDate: Option[Instant] = None ): Unit = {
    val (start, end) =  (fromDate, toDate) match {
      case (Some(startDate), Some(endDate)) => (startDate, endDate)
      case _ => (getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS)),
        Instant.now.minus(1, ChronoUnit.MINUTES))
    }

    logger.println(s"start: $start")
    logger.println(s"end: $end")

    val assetType = Seq(AssetType("mass_transit_stops", "MassTransitStop", "MassTransitStop", "", BusStopOTHClient, new PointRosatteConverter))

    val assets = fetchAllChanges(start, end, assetType)

    logger.println("fetched Bus Stops changes to generate Vallu XML, generating dataset")

    val dataSetId = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
    val filename = s"${URLEncoder.encode(dataSetId, "UTF-8")}.xml"

    try {
      // Create new stream to the SFTP server
      val outputStreamSFTP = getOutputStreamSFTP(filename)

      try {
        BusStopValluConverter.convertDataSet(assetType.zip(assets), outputStreamSFTP)
      } finally {
        outputStreamSFTP.close()
      }
    } catch {
      case e: Throwable => logger.println("SFTP OutputStream  Failed with the follow message: ", e.getMessage)
    }

    logger.println(s"Dataset ID: $dataSetId")
    logger.println(s"dataset: $filename")
    logger.println("done!\n")
  }
}

class StdConverter extends Converter {
  override def assetTypes: Seq[AssetType] = Seq(
    AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph", OTHClient, new LinearRosatteConverter),
    AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "cm", OTHClient, new LinearRosatteConverter),
    AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "cm", OTHClient, new LinearRosatteConverter),
    AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "cm", OTHClient, new LinearRosatteConverter),
    AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg", OTHClient, new LinearRosatteConverter),
    //AssetType("road_names", "RoadName", "RoadName", ""),
    AssetType("road_numbers", "RoadNumber", "RoadNumber", "", ViiteClient, new LinearRosatteConverter),
    AssetType("vehicle_prohibitions", "NoEntry", "NoEntry", "", VehicleOTHClient, new LinearRosatteConverter),
    AssetType("pedestrian_crossing", "PedestrianCrossing", "PedestrianCrossing", "", PedestrianCrossingOTHClient, new PointRosatteConverter),
    AssetType("obstacles", "ClosedToAllVehiclesInBothDirection", "ClosedToAllVehiclesInBothDirection", "", ObstacleOTHClient, new PointRosatteConverter),
    AssetType("warning_signs_group", "WarningSign", "WarningSignType", "", WarningSignOTHClient, new PointValueRosatteConverter, "FixedTrafficSign"),
    AssetType("stop_sign", "PassingWithoutStoppingProhibited", "PassingWithoutStoppingProhibited", "", StopSignOTHClient, new PointRosatteConverter)
  )

  override def directory: String = config.baseDirSFTP
  override def getLatestEndTime: Option[Instant] = RemoteDataset.getLatestEndTime
  override def getOutputStreamSFTP(filename : String): OutputStream = RemoteDataset.getOutputStreamSFTP(filename, directory)
}

class NonStdConverter extends Converter {
  override def assetTypes: Seq[AssetType] = Seq(
    AssetType("trailer_truck_weight_limits", "RestrictionForVehicles", "MaximumTrailerTruckWeight", "kg", OTHClient, new LinearRosatteConverter),
    AssetType("bogie_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg", BogieWeightLimitOTHClient, new BogieWeightLimitRosatteConverter)
  )

  override def directory: String = config.nonStdDirSFTP
  override def getLatestEndTime: Option[Instant] = RemoteNonStdDataset.getLatestEndTime
  override def getOutputStreamSFTP(filename : String): OutputStream = RemoteNonStdDataset.getOutputStreamSFTP(filename, directory)

}

object TestConverterObject {
  /** Runs a conversion from the command line */
  def main(args: Array[String]): Unit = {
    val stdConverter = new StdConverter
    stdConverter.convert(new PrintWriter(System.out, true))

    val busStopConverter = new BusStopConverter
    busStopConverter.convert(new PrintWriter(System.out, true))

    val nonStdConverter = new NonStdConverter
    nonStdConverter.convert(new PrintWriter(System.out, true))
    System.exit(0) //TODO: Without this, something leaves the program hanging
  }
}
package fi.liikennevirasto.digiroad2.tnits.runners

import java.io.PrintWriter
import java.net.URLEncoder
import java.time.Instant
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
import java.util.Base64


case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String, client: Client, service: AssetRosatteConverter, source: String = "Regulation")

/** Base class to run conversion batch job. */
trait Converter {
  implicit def dateTimeOrdering: Ordering[Instant] = Ordering.fromLessThan(_ isBefore _ )
  def assetTypes: Seq[AssetType]
  def directory: String
  def getLatestEndTime: Option[Instant]
  def getOutputStreamSFTP(filename : String): OutputStream

  private val LIMIT_RECORD_NUMBER = 20000

  case class OTHException(cause: Throwable) extends RuntimeException(cause)

  def recursiveCall(start: Instant, end: Instant, assetTypes: Seq[AssetType], pageNumber: Option[Int] = Some(1),
                    result: Seq[(AssetType, Seq[Feature[AssetProperties]])] = Seq(), logger: PrintWriter): Seq[(AssetType, Seq[Feature[AssetProperties]])] = {

    val message = s"pageNumber:${pageNumber.get}, recordNumber:$LIMIT_RECORD_NUMBER"
    val token = Some(Base64.getEncoder.encodeToString(message.getBytes("UTF-8")))

    val assets = fetchAllChanges(start, end, assetTypes, token, logger = logger)
    val zippedAsset: Seq[(AssetType, Seq[Feature[AssetProperties]])] = assetTypes.zip(assets)

    val oversizeAssetTypes = zippedAsset.foldLeft(Seq.empty[AssetType]) { case (res, asset) =>
      if (asset._2.size == LIMIT_RECORD_NUMBER && !asset._1.apiEndPoint.equals("road_numbers")) res :+ asset._1 else res
    }

    if(oversizeAssetTypes.nonEmpty)
      recursiveCall(start, end, oversizeAssetTypes, Some(pageNumber.get + 1), result ++ zippedAsset, logger = logger)
    else {
      result ++ zippedAsset
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

    val assets = recursiveCall(start, end, assetTypes, logger = logger).groupBy(_._1).flatten(_._2).toSeq

    logger.println(s"fetched all changes, generating dataset for assets")
    assets.foreach{ asset =>
      logger.println(s"for endPoint ${asset._1.apiEndPoint} with the size ${asset._2.size}")
    }

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
      case e: Throwable =>
        logger.println("SFTP OutputStream  Failed with the follow message: ", e.getMessage)
        logger.println(e)
    }

    logger.println(s"Dataset ID: $dataSetId")
    logger.println(s"dataset: $filename")
    logger.println("done!\n")
  }

  protected def fetchAllChanges(start: Instant, end: Instant, assetTypes: Seq[AssetType], token: Option[String] = None, logger: PrintWriter): Seq[Seq[Feature[AssetProperties]]] = {
    try {
      val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
      val responses = Future.sequence(assetTypes.map { asset =>
        asset.client.fetchChanges(asset.apiEndPoint, start, end, token, executor, logger)
      })
      Await.result(responses, 120.minutes)
    } catch {
      case err: Throwable =>
        logger.println(s"Error in fetchAllChanges $err")
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

    val assets = fetchAllChanges(start, end, assetType, logger = logger)

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
    //AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph", OTHClient, new LinearRosatteConverter),
    //AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "cm", OTHClient, new LinearRosatteConverter),
    //AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "cm", OTHClient, new LinearRosatteConverter),
    //AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "cm", OTHClient, new LinearRosatteConverter),
    //AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg", OTHClient, new LinearRosatteConverter),
    //AssetType("road_names", "RoadName", "RoadName", ""),
    //AssetType("road_numbers", "RoadNumber", "RoadNumber", "", ViiteClient, new LinearRosatteConverter),
    //AssetType("vehicle_prohibitions", "NoEntry", "NoEntry", "", VehicleOTHClient, new LinearRosatteConverter),
    //AssetType("pedestrian_crossing", "PedestrianCrossing", "PedestrianCrossing", "", PedestrianCrossingOTHClient, new PointRosatteConverter),
    //AssetType("obstacles", "ClosedToAllVehiclesInBothDirection", "ClosedToAllVehiclesInBothDirection", "", ObstacleOTHClient, new PointRosatteConverter),
    AssetType("warning_signs_group", "WarningSign", "WarningSignType", "", WarningSignOTHClient, new PointValueRosatteConverter, "FixedTrafficSign")
    //AssetType("stop_sign", "PassingWithoutStoppingProhibited", "PassingWithoutStoppingProhibited", "", StopSignOTHClient, new PointRosatteConverter)
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
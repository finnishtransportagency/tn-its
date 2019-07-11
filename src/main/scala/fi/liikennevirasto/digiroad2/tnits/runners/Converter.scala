package fi.liikennevirasto.digiroad2.tnits.runners

import java.io.PrintWriter
import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.geojson.Feature
import fi.liikennevirasto.digiroad2.tnits.oth._
import fi.liikennevirasto.digiroad2.tnits.rosatte._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String, client: Client, service: AssetRosatteConverter, source: String = "Regulation")


/** Runs a conversion batch job. */
object Converter {
  /** Runs a conversion from the commandline. */
  def main(args: Array[String]) {
    convert(new PrintWriter(System.out, true))
    System.exit(0) // TODO: Without this, something leaves the program hanging
  }

  case class OTHException(cause: Throwable) extends RuntimeException(cause)

  /** Runs a conversion programmatically. */
  def convert(logger: PrintWriter, fromDate: Option[Instant] = None, toDate: Option[Instant] = None ): Unit = {
    val (start, end) =  (fromDate, toDate) match {
      case (Some(startDate), Some(endDate)) => (startDate, endDate)
      case _ => (RemoteDatasets.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS)),
        Instant.now.minus(1, ChronoUnit.MINUTES))
    }

    logger.println(s"start: $start")
    logger.println(s"end: $end")

    val assetTypes = Seq(
      AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph", OTHClient, LinearRosatteConverter),
      AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg", OTHClient, LinearRosatteConverter),
      AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "cm", OTHClient, LinearRosatteConverter),
      AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "cm", OTHClient, LinearRosatteConverter),
      AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "cm", OTHClient, LinearRosatteConverter),
      AssetType("total_weight_limits", "RestrictionForVehicles", "MaximumLadenWeight", "kg", OTHClient, LinearRosatteConverter),
//      AssetType("road_names", "RoadName", "RoadName", ""),
      AssetType("road_numbers", "RoadNumber", "RoadNumber", "", ViiteClient, LinearRosatteConverter),
      AssetType("vehicle_prohibitions", "NoEntry", "NoEntry", "", VehicleOTHClient, LinearRosatteConverter),
      AssetType("pedestrian_crossing", "PedestrianCrossing", "PedestrianCrossing", "", PedestrianCrossingOTHClient, new PointRosatteConverter),
      AssetType("obstacles", "ClosedToAllVehiclesInBothDirection", "ClosedToAllVehiclesInBothDirection", "", ObstacleOTHClient, new PointRosatteConverter),
      AssetType("warning_signs_group", "WarningSign", "WarningSignType", "", WarningSignOTHClient, new PointValueRosatteConverter, "FixedTrafficSign"),
      AssetType("stop_sign", "PassingWithoutStoppingProhibited", "PassingWithoutStoppingProhibited", "", StopSignOTHClient, new PointRosatteConverter))
    val assets = fetchAllChanges(start, end, assetTypes)
    logger.println("fetched all changes, generating dataset")

    val dataSetId = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
    val filename = s"${URLEncoder.encode(dataSetId, "UTF-8")}.xml"

    try {
      // Create new stream to the SFTP server for replace a stream to the FTP server in the Future
      val outputStreamSFTP = RemoteDatasets.getOutputStreamSFTP(filename)

      try {
        RosatteConverter.convertDataSet(assetTypes.zip(assets), start, end, dataSetId, outputStreamSFTP)
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

  /** Runs a conversion programmatically. */
  def convertBusStopOnXml(logger: PrintWriter, fromDate: Option[Instant] = None, toDate: Option[Instant] = None ): Unit = {
    val (start, end) =  (fromDate, toDate) match {
      case (Some(startDate), Some(endDate)) => (startDate, endDate)
      case _ => (RemoteDatasets.getLatestEndTimeOnFolder.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS)),
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
      val outputStreamSFTP = RemoteDatasets.getOutputStreamSFTP(filename, config.baseDirBusStopsSFTP)

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

  private def fetchAllChanges(start: Instant, end: Instant, assetTypes: Seq[AssetType]): Seq[Seq[Feature[AssetProperties]]] = {
    try {
      val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
      val responses = Future.sequence(assetTypes.map { asset =>
        asset.client.fetchChanges(asset.apiEndPoint, start, end, executor)
      })
      Await.result(responses, 120.minutes)
    } catch {
      case err: Throwable =>
        throw OTHException(err)
    }
  }
}

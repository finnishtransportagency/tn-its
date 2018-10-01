package fi.liikennevirasto.digiroad2.tnits.runners

import java.io.PrintWriter
import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.geojson.Feature
import fi.liikennevirasto.digiroad2.tnits.oth.{Client, OTHClient, VehicleOTHClient, ViiteClient}
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.{Asset, VehicleProhibitionAssetProperties}
import fi.liikennevirasto.digiroad2.tnits.rosatte.{AssetProperties, DatasetID, RosatteConverter, features}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String, client: Client)

/** Runs a conversion batch job. */
object Converter {
  /** Runs a conversion from the commandline. */
  def main(args: Array[String]) {
    convert(new PrintWriter(System.out, true), fromDate = Some(Instant.now.minus(7, ChronoUnit.DAYS)), toDate = Some(Instant.now))
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
      AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph", OTHClient),
      AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg", OTHClient),
      AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "cm", OTHClient),
      AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "cm", OTHClient),
      AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "cm", OTHClient),
      AssetType("total_weight_limits", "RestrictionForVehicles", "MaximumLadenWeight", "kg", OTHClient),
//      AssetType("road_names", "RoadName", "RoadName", ""),
      AssetType("road_numbers", "RoadNumber", "RoadNumber", "", ViiteClient),
      AssetType("vehicle_prohibitions", "NoEntry", "NoEntry", "", VehicleOTHClient))

    val assets = fetchAllChanges(start, end, assetTypes)
    logger.println("fetched all changes, generating dataset")

    val dataSetId = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
    val filename = s"${URLEncoder.encode(dataSetId, "UTF-8")}.xml"

    try {
//       Create new stream to the SFTP server for replace a stream to the FTP server in the Future
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

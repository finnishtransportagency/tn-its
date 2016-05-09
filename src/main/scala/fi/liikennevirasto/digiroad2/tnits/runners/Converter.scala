package fi.liikennevirasto.digiroad2.tnits.runners

import java.io.PrintWriter
import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.oth.OTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.{DatasetID, RosatteConverter, features}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String)

object Converter {
  def main(args: Array[String]) {
    convert(new PrintWriter(System.out, true))
    System.exit(0) // TODO: Without this, something leaves the program hanging
  }

  case class OTHException(cause: Throwable) extends RuntimeException(cause)

  def convert(logger: PrintWriter): Unit = {
    val start = RemoteDatasets.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS))
    val end = Instant.now.minus(1, ChronoUnit.MINUTES)

    logger.println(s"start: $start")
    logger.println(s"end: $end")

    val assetTypes = Seq(
      AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"),
      AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg"),
      AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "cm"),
      AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "cm"),
      AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "cm"),
      AssetType("total_weight_limits", "RestrictionForVehicles", "MaximumLadenWeight", "kg"))

    val assets = fetchAllChanges(start, end, assetTypes)
    logger.println("fetched all changes, generating dataset")

    val dataSetId = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
    val filename = s"${URLEncoder.encode(dataSetId, "UTF-8")}.xml"
    val outputStream = RemoteDatasets.getOutputStream(filename)
    RosatteConverter.convertDataSet(assetTypes.zip(assets), start, end, dataSetId, outputStream)
    logger.println(s"Dataset ID: $dataSetId")
    logger.println(s"dataset: $filename")
    outputStream.close()
    logger.println("done!\n")
  }

  def fetchAllChanges(start: Instant, end: Instant, assetTypes: Seq[AssetType]): Seq[Seq[features.Asset]] = {
    try {
      val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
      val responses = Future.sequence(assetTypes.map { asset =>
        OTHClient.fetchChanges(asset.apiEndPoint, start, end, executor)
      })
      Await.result(responses, 15.minutes)
    } catch {
      case err: Throwable =>
        throw OTHException(err)
    }
  }
}

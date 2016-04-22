package fi.liikennevirasto.digiroad2.tnits.runners

import java.io.PrintWriter
import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executors

import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.oth.OTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.{RosatteConverter, features}

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

  def convert(writer: PrintWriter): Unit = {
    val start = RemoteDatasets.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS))
    val end = Instant.now.minus(1, ChronoUnit.MINUTES)

    writer.println(s"start: $start")
    writer.println(s"end: $end")

    val assetTypes = Seq(
      AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"),
      AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg"),
      AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "kg"),
      AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "kg"),
      AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "kg"),
      AssetType("total_weight_limits", "RestrictionForVehicles", "MaximumLadenWeight", "kg"))

    val assets = fetchAllChanges(start, end, assetTypes)

    val allRosatteFeatures =
      assetTypes.zip(assets).flatMap { case (assetType, changes) =>
        RosatteConverter.convert(changes, assetType.featureType, assetType.valueType, assetType.unit)
      }

    val dataSet = RosatteConverter.generateDataSet(allRosatteFeatures, start, end)
    val filename = s"${URLEncoder.encode(dataSet.id, "UTF-8")}.xml"
    writer.println(s"Dataset ID: ${dataSet.id}")
    writer.println(s"dataset: $filename")
    RemoteDatasets.put(filename, dataSet.updates)
    writer.println("done!\n")
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

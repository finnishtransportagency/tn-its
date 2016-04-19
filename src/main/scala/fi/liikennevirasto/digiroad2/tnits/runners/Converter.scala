package fi.liikennevirasto.digiroad2.tnits.runners

import java.net.URLEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit

import dispatch.Http
import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.oth.OTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.RosatteConverter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class AssetType(apiEndPoint: String, featureType: String, valueType: String, unit: String)

object Converter {
  def main(args: Array[String]) {
    try {
      convert()
    } finally {
      Http.shutdown()
    }
  }

  def convert(): Unit = {
    val start = RemoteDatasets.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS))
    val end = Instant.now.minus(1, ChronoUnit.MINUTES)

    val assetTypes = Seq(
      AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"),
      AssetType("axle_weight_limits", "RestrictionForVehicles", "MaximumWeightPerSingleAxle", "kg"),
      AssetType("length_limits", "RestrictionForVehicles", "MaximumLength", "kg"),
      AssetType("width_limits", "RestrictionForVehicles", "MaximumWidth", "kg"),
      AssetType("height_limits", "RestrictionForVehicles", "MaximumHeight", "kg"),
      AssetType("total_weight_limits", "RestrictionForVehicles", "MaximumLadenWeight", "kg"))

    val assets = Await.result(
      Future.sequence(assetTypes.map(asset => OTHClient.fetchChanges(asset.apiEndPoint, start, end))),
      30.seconds)
    val allRosatteFeatures =
      assetTypes.zip(assets).flatMap { case (assetType, changes) =>
        RosatteConverter.convert(changes, assetType.featureType, assetType.valueType, assetType.unit)
      }

    val dataSet = RosatteConverter.generateDataSet(allRosatteFeatures, start, end)
    println(dataSet.updates)
    val filename = s"${URLEncoder.encode(dataSet.id, "UTF-8")}.xml"
    RemoteDatasets.put(filename, dataSet.updates)
  }
}

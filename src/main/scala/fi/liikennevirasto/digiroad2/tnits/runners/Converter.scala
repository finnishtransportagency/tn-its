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

object Converter {
  def main(args: Array[String]) {
    try {
      val start = RemoteDatasets.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS))
      val end = Instant.now.minus(1, ChronoUnit.MINUTES)
      val Seq(speedLimits, totalWeightLimits) =
        Await.result(
          Future.sequence(Seq(
            OTHClient.readSpeedLimitChanges(start, end),
            OTHClient.readTotalWeightLimitChanges(start, end))),
          30.seconds)
      val allRosatteFeatures =
        RosatteConverter.convert(speedLimits, "SpeedLimit", "MaximumSpeedLimit", "kmph") ++
        RosatteConverter.convert(totalWeightLimits, "RestrictionForVehicles", "MaximumLadenWeight", "kg")
      val dataSet = RosatteConverter.generateDataSet(allRosatteFeatures, start, end)
      println(dataSet.updates)
      val filename = s"${URLEncoder.encode(dataSet.id, "UTF-8")}.xml"
      RemoteDatasets.put(filename, dataSet.updates)
    } finally {
      Http.shutdown()
    }
  }
}

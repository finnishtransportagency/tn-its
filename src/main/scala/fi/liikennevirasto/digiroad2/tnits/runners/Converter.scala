package fi.liikennevirasto.digiroad2.tnits.runners

import java.net.URLEncoder
import java.time.Instant

import dispatch.Http
import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.oth.OTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.RosatteConverter

object Converter {
  def main(args: Array[String]) {
    try {
      val start = Instant.parse("2016-04-07T09:30:00Z")
      val end = Instant.now
      val speedLimits = OTHClient.readSpeedLimitChanges(start)
      val dataSet = RosatteConverter.convert(speedLimits, start, end)
      println(dataSet.updates)
      val filename = s"${URLEncoder.encode(dataSet.id, "UTF-8")}.xml"
      RemoteDatasets.put(filename, dataSet.updates)
    } finally {
      Http.shutdown()
    }
  }
}

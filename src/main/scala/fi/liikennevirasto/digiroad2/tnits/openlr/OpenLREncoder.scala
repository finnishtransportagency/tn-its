package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util.Base64

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import openlr.binary.ByteArray
import openlr.encoder.OpenLREncoderParameter
import openlr.location.LocationFactory

object OpenLREncoder {
  def encodeAssetOnLink(startMeasure: Double, endMeasure: Double, linkGeometry: Seq[Point], linkLength: Double, functionalClass: Int, linkType: Int): String = {
    import collection.JavaConverters._

    val line = DigiroadLine(1, linkGeometry, linkLength.floor.toInt, linkType.intValue, functionalClass.intValue)

    val mapDatabase = new DigiroadFixtureMapDatabase(Seq(line))
    val encoder = new openlr.encoder.OpenLREncoder
    val param = new OpenLREncoderParameter.Builder()
      .`with`(mapDatabase)
      .buildParameter()

    val lineLocation =
      LocationFactory.createLineLocationWithOffsets(
        s"loc-1", Seq(line).asJava, startMeasure.floor.toInt, (linkLength - endMeasure).floor.toInt)
    val encoded = encoder.encodeLocation(param, lineLocation)
    val reference = encoded.getLocationReference("binary")
    val data = reference.getLocationReferenceData.asInstanceOf[ByteArray]
    new String(Base64.getEncoder.encode(data.getData), "ASCII")
  }
}

package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util.Base64

import openlr.binary.ByteArray
import openlr.encoder.{OpenLREncoder, OpenLREncoderParameter}
import openlr.location.LocationFactory

object OpenLRTest {

  def main(args: Array[String]) = {
    import collection.JavaConverters._

    val mapDatabase = new DigiroadMapDatabase
    val encoder = new OpenLREncoder
    val param = new OpenLREncoderParameter.Builder()
      .`with`(mapDatabase)
      .buildParameter()

    val lines = mapDatabase.getAllLines.asScala
    lines.foreach { line =>
      val lineLocation = LocationFactory.createLineLocation("loc", Seq(line).asJava)
      val encoded = encoder.encodeLocation(param, lineLocation)
      val reference = encoded.getLocationReference("binary")
      val data = reference.getLocationReferenceData.asInstanceOf[ByteArray]
      println(new String(Base64.getEncoder.encode(data.getData), "ASCII"))
    }
  }
}

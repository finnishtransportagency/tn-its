package fi.liikennevirasto.digiroad2.tnits.openlr.examples

import java.util.Base64

import fi.liikennevirasto.digiroad2.tnits.Point
import fi.liikennevirasto.digiroad2.tnits.openlr.{DigiroadFixtureMapDatabase, DigiroadLine}
import openlr.binary.ByteArray
import openlr.encoder.{OpenLREncoder, OpenLREncoderParameter}
import openlr.location.LocationFactory
import openlr.xml.OpenLRXmlWriter
import openlr.xml.generated.OpenLR

object OpenLRExample {

  def main(args: Array[String]) = {
    import collection.JavaConverters._

    val mapDatabase = new DigiroadFixtureMapDatabase(Seq(
      DigiroadLine(1, Seq(Point(0, 0), Point(10, 0)), 10),
      DigiroadLine(2, Seq(Point(10, 0), Point(30, 0)), 20)
    ))
    val encoder = new OpenLREncoder
    val param = new OpenLREncoderParameter.Builder()
      .`with`(mapDatabase)
      .buildParameter()

    val lines = mapDatabase.getAllLines.asScala
    lines.zipWithIndex.foreach { case (line, i) =>
      val lineLocation = LocationFactory.createLineLocationWithOffsets(s"loc-$i", Seq(line).asJava, 2, 2)
      val encoded = encoder.encodeLocation(param, lineLocation)
      val reference = encoded.getLocationReference("binary")
      val data = reference.getLocationReferenceData.asInstanceOf[ByteArray]
      println("Binary: " + new String(Base64.getEncoder.encode(data.getData), "ASCII"))
      println("XML:")
      val writer = new OpenLRXmlWriter
      writer.saveOpenLRXML(
        encoded.getLocationReference("xml").getLocationReferenceData.asInstanceOf[OpenLR],
        System.out,
        true
      )
    }
  }
}

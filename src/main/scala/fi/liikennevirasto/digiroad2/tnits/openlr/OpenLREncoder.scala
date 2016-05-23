package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util.Base64

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import openlr.binary.ByteArray
import openlr.encoder.{OpenLREncoderParameter, OpenLREncoderProcessingException}
import openlr.location.LocationFactory

import scala.util.Try

/** OpenLR encoding using TomTom's library.
  *
  * @see [[http://www.openlr.org/software.html]]
  */
object OpenLREncoder {
  /** @return the Base64 encoded string representing the OpenLR encoding result of the given startMeasure and endMeasure on the given link geometry.
    */
  def encodeAssetOnLink(startMeasure: Double, endMeasure: Double, linkGeometry: Seq[Point], linkLength: Double, functionalClass: Int, linkType: Int, linkId: String): Try[String] = {
    import collection.JavaConverters._

    Try {
      val line = DigiroadLine(1, linkGeometry, linkLength.floor.toInt, linkType.intValue, functionalClass.intValue)

      val mapDatabase = new DigiroadFixtureMapDatabase(Seq(line))
      val encoder = new openlr.encoder.OpenLREncoder
      val param = new OpenLREncoderParameter.Builder()
        .`with`(mapDatabase)
        .buildParameter()

      val realEndMeasure = if (linkLength > endMeasure) linkLength - endMeasure else 0
      val lineLocation =
        LocationFactory.createLineLocationWithOffsets(
          linkId, Seq(line).asJava, startMeasure.floor.toInt, realEndMeasure.floor.toInt)

      val encoded = try
        encoder.encodeLocation(param, lineLocation)
      catch {
        case e: openlr.encoder.OpenLREncoderProcessingException =>
          throw new OpenLRException(startMeasure, endMeasure, linkGeometry, linkLength, functionalClass, linkType, e)

      }
      val reference = encoded.getLocationReference("binary")
      val data = reference.getLocationReferenceData.asInstanceOf[ByteArray]
      new String(Base64.getEncoder.encode(data.getData), "ASCII")
    }
  }
}

case class OpenLRException(
  startMeasure: Double,
  endMeasure: Double,
  linkGeometry: Seq[Point],
  linkLength: Double,
  functionalClass: Int,
  linkType: Int,
  e: OpenLREncoderProcessingException) extends Throwable(e) {
  override def toString: String =
    s"OpenLRException(startMeasure = $startMeasure, endMeasure = $endMeasure, linkGeometry = ${linkGeometry.toList}, linkLength = $linkLength, functionalClass = $functionalClass, linkType = $linkType)"
}


package fi.liikennevirasto.digiroad2.tnits.openlr

import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.rosatte.RosatteConverter
import openlr.map.GeoCoordinates

/**
  * Implements the coordinates in TomTom's OpenLR library
  * Represents the coordinates in wgs84 format
  */

case class DigiroadCoordinates(point: Point) extends GeoCoordinates {
  val wgs84LonLat = CoordinateTransform.convertToWgs84(Seq(point.x, point.y))

  override def getLongitudeDeg: Double =
    wgs84LonLat(0)

  override def getLatitudeDeg: Double =
    wgs84LonLat(1)
}

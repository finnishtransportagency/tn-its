package fi.liikennevirasto.digiroad2.tnits.openlr

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import fi.liikennevirasto.digiroad2.tnits.rosatte.RosatteConverter
import openlr.map.GeoCoordinates

case class DigiroadCoordinates(point: Point) extends GeoCoordinates {
  val wgs84LonLat = RosatteConverter.convertCoordinates(Seq(point.x, point.y))

  override def getLongitudeDeg: Double =
    wgs84LonLat(0)

  override def getLatitudeDeg: Double =
    wgs84LonLat(1)
}

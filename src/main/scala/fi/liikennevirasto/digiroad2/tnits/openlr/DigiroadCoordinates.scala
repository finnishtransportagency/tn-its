package fi.liikennevirasto.digiroad2.tnits.openlr

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import openlr.map.GeoCoordinates

case class DigiroadCoordinates(point: Point) extends GeoCoordinates {
  override def getLongitudeDeg: Double =
    point.x

  override def getLatitudeDeg: Double =
    point.y
}

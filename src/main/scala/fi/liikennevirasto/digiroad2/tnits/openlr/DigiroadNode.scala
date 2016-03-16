package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util

import fi.liikennevirasto.digiroad2.tnits.Point
import openlr.map.{GeoCoordinates, Line, Node}

case class DigiroadNode(point: Point) extends Node {
  override def getLongitudeDeg: Double =
    point.x

  override def getLatitudeDeg: Double =
    point.y

  override def getNumberConnectedLines: Int =
    1

  override def getID: Long =
    ???

  override def getConnectedLines: util.Iterator[Line] =
    ???

  override def getOutgoingLines: util.Iterator[Line] =
    ???

  override def getGeoCoordinates: GeoCoordinates =
    DigiroadCoordinates(point)

  override def getIncomingLines: util.Iterator[Line] =
    ???
}

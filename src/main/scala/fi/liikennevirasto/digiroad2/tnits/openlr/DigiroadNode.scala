package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import openlr.map.{GeoCoordinates, Line, Node}

case class DigiroadNode(coordinates: DigiroadCoordinates) extends Node {
  override def getLongitudeDeg: Double =
    coordinates.getLongitudeDeg

  override def getLatitudeDeg: Double =
    coordinates.getLatitudeDeg

  override def getNumberConnectedLines: Int =
    1

  override def getGeoCoordinates: GeoCoordinates =
    coordinates

  override def getID: Long =
    ???

  override def getConnectedLines: util.Iterator[Line] =
    ???

  override def getOutgoingLines: util.Iterator[Line] =
    ???

  override def getIncomingLines: util.Iterator[Line] =
    ???
}

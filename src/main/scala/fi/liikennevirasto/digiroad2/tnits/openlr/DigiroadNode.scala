package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import openlr.map.{GeoCoordinates, Line, Node}

/**
  * Implements the Digiroad Node for TomTom's OpenLR library
  *
  * Nodes are "pseudo nodes" in a sence, they only present the ends of a single road link and do not know
  * of the other neighboring links. They are only needed because the TomTom's OpenLR libarary requires the Nodes.
  * This is sufficient as the Digiroad attributes never exceed length of a single link.
  */

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

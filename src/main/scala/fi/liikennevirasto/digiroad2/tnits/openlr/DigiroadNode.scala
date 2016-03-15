package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util

import openlr.map.{GeoCoordinates, GeoCoordinatesImpl, Line, Node}

case class DigiroadNode(line: Line) extends Node {
  import collection.JavaConverters._

  override def getID: Long = ???

  override def getLongitudeDeg: Double = 0

  override def getLatitudeDeg: Double = 0

  override def getConnectedLines: util.Iterator[Line] = Seq(line).iterator.asJava

  override def getOutgoingLines: util.Iterator[Line] = ???

  override def getGeoCoordinates: GeoCoordinates = new GeoCoordinatesImpl(0, 0)

  override def getIncomingLines: util.Iterator[Line] = ???

  override def getNumberConnectedLines: Int = 1
}

package fi.liikennevirasto.digiroad2.tnits.openlr

import java.util

import fi.liikennevirasto.digiroad2.tnits.Point
import openlr.map.{GeoCoordinates, Line, Node}

case class DigiroadNode(point: Point) extends Node {
  import collection.JavaConverters._

  private var line: Line = null

  def setConnectedLine(line: Line): Unit = {
    this.line = line
  }

  override def getID: Long = ???

  override def getLongitudeDeg: Double = point.x

  override def getLatitudeDeg: Double = point.y

  override def getConnectedLines: util.Iterator[Line] = Seq(line).iterator.asJava

  override def getOutgoingLines: util.Iterator[Line] = ???

  override def getGeoCoordinates: GeoCoordinates = DigiroadCoordinates(point)

  override def getIncomingLines: util.Iterator[Line] = ???

  override def getNumberConnectedLines: Int = 1
}

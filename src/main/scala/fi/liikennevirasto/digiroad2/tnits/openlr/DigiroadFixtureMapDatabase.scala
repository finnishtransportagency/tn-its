package fi.liikennevirasto.digiroad2.tnits.openlr

import java.awt.geom.Rectangle2D
import java.util

import openlr.map.{Line, MapDatabase, Node}

/**
  * Implements the MapDatabase for the TomTom's OpenLR library
  */
case class DigiroadFixtureMapDatabase(lines: Seq[Line]) extends MapDatabase {
  import collection.JavaConverters._

  override def getLine(id: Long): Line =
    lines.find { line => line.getID == id }.orNull

  override def getAllLines: util.Iterator[Line] =
    lines.iterator.asJava

  override def hasTurnRestrictions: Boolean =
    ???

  override def getAllNodes: util.Iterator[Node] =
    ???

  override def hasTurnRestrictionOnPath(path: util.List[_ <: Line]): Boolean =
    ???

  override def findNodesCloseByCoordinate(longitude: Double, latitude: Double, distance: Int): util.Iterator[Node] =
    ???

  override def findLinesCloseByCoordinate(longitude: Double, latitude: Double, distance: Int): util.Iterator[Line] =
    ???

  override def getMapBoundingBox: Rectangle2D.Double =
    ???

  override def getNumberOfLines: Int =
    ???

  override def getNumberOfNodes: Int =
    ???

  override def getNode(id: Long): Node =
    ???
}

package fi.liikennevirasto.digiroad2.tnits.openlr

import java.awt.geom.Rectangle2D
import java.util

import fi.liikennevirasto.digiroad2.tnits.Point
import openlr.map.{Line, MapDatabase, Node}

class DigiroadFixtureMapDatabase extends MapDatabase {
  import collection.JavaConverters._

  override def hasTurnRestrictions: Boolean = ???

  override def getAllNodes: util.Iterator[Node] = ???

  override def getLine(id: Long): Line = ???

  override def getAllLines: util.Iterator[Line] =
    Seq[Line](
      DigiroadLine(1, Seq(Point(0, 0), Point(10, 0)), 10),
      DigiroadLine(2, Seq(Point(10, 0), Point(30, 0)), 20)
    ).iterator.asJava

  override def hasTurnRestrictionOnPath(path: util.List[_ <: Line]): Boolean = ???

  override def findNodesCloseByCoordinate(longitude: Double, latitude: Double, distance: Int): util.Iterator[Node] = ???

  override def findLinesCloseByCoordinate(longitude: Double, latitude: Double, distance: Int): util.Iterator[Line] = ???

  override def getMapBoundingBox: Rectangle2D.Double = ???

  override def getNumberOfLines: Int = ???

  override def getNumberOfNodes: Int = ???

  override def getNode(id: Long): Node = ???
}

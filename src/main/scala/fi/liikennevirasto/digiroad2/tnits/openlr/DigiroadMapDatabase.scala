package fi.liikennevirasto.digiroad2.tnits.openlr

import java.awt.geom.Rectangle2D
import java.util

import openlr.map.{Line, MapDatabase, Node}

class DigiroadMapDatabase extends MapDatabase {
  import collection.JavaConverters._

  override def hasTurnRestrictions: Boolean = ???

  override def getAllNodes: util.Iterator[Node] = ???

  override def getLine(id: Long): Line = ???

  override def getAllLines: util.Iterator[Line] =
    Seq[Line](
      DigiroadLine(1, 10),
      DigiroadLine(2, 20)
    ).iterator.asJava

  override def hasTurnRestrictionOnPath(path: util.List[_ <: Line]): Boolean = ???

  override def findNodesCloseByCoordinate(longitude: Double, latitude: Double, distance: Int): util.Iterator[Node] = ???

  override def findLinesCloseByCoordinate(longitude: Double, latitude: Double, distance: Int): util.Iterator[Line] = ???

  override def getMapBoundingBox: Rectangle2D.Double = ???

  override def getNumberOfLines: Int = ???

  override def getNumberOfNodes: Int = ???

  override def getNode(id: Long): Node = ???
}

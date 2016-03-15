package fi.liikennevirasto.digiroad2.tnits.openlr

import java.awt.geom.{Path2D, Point2D}
import java.util
import java.util.Locale

import DigiroadNode
import openlr.map._

case class DigiroadLine(id: Long, length: Int) extends Line {
  override def getGeoCoordinateAlongLine(distanceAlong: Int): GeoCoordinates = ???

  override def getID: Long = id

  override def getShape: Path2D.Double = ???

  override def getLineLength: Int = length

  override def getPointAlongLine(distanceAlong: Int): Point2D.Double = ???

  override def getFRC: FunctionalRoadClass = FunctionalRoadClass.FRC_0

  override def getNextLines: util.Iterator[Line] = ???

  override def getShapeCoordinates: util.List[GeoCoordinates] = ???

  override def getStartNode: Node = DigiroadNode(this)

  override def getEndNode: Node = DigiroadNode(this)

  override def getNames: util.Map[Locale, util.List[String]] = ???

  override def measureAlongLine(longitude: Double, latitude: Double): Int = ???

  override def getPrevLines: util.Iterator[Line] = ???

  override def distanceToPoint(longitude: Double, latitude: Double): Int = ???

  override def getFOW: FormOfWay = FormOfWay.MOTORWAY
}

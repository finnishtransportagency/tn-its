package fi.liikennevirasto.digiroad2.tnits.openlr

import java.awt.geom.{Path2D, Point2D}
import java.util
import java.util.Locale

import fi.liikennevirasto.digiroad2.tnits.Point
import openlr.map._

case class DigiroadLine(id: Long, geometry: Seq[Point], length: Int) extends Line {
  override def getGeoCoordinateAlongLine(distanceAlong: Int): GeoCoordinates =
    DigiroadCoordinates(calculatePointFromLinearReference(geometry, distanceAlong.toDouble).get)

  override def getID: Long =
    id

  override def getLineLength: Int =
    length

  override def getFRC: FunctionalRoadClass =
    FunctionalRoadClass.FRC_0

  override def getStartNode: Node =
    DigiroadNode(geometry(0))

  override def getEndNode: Node =
    DigiroadNode(geometry.last)

  override def getFOW: FormOfWay =
    FormOfWay.MOTORWAY

  override def getShape: Path2D.Double =
    ???

  override def getPointAlongLine(distanceAlong: Int): Point2D.Double =
    ???

  override def getNextLines: util.Iterator[Line] =
    ???

  override def getShapeCoordinates: util.List[GeoCoordinates] =
    ???

  override def getNames: util.Map[Locale, util.List[String]] =
    ???

  override def measureAlongLine(longitude: Double, latitude: Double): Int =
    ???

  override def getPrevLines: util.Iterator[Line] =
    ???

  override def distanceToPoint(longitude: Double, latitude: Double): Int =
    ???

  private def calculatePointFromLinearReference(geometry: Seq[Point], measure: Double): Option[Point] = {
    case class AlgorithmState(previousPoint: Point, remainingMeasure: Double, result: Option[Point])
    if (geometry.size < 2 || measure < 0) { None }
    else {
      val state = geometry.tail.foldLeft(AlgorithmState(geometry.head, measure, None)) { (acc, point) =>
        if (acc.result.isDefined) {
          acc
        } else {
          val distance = point.distanceTo(acc.previousPoint)
          val remainingMeasure = acc.remainingMeasure
          if (remainingMeasure <= distance + 0.01) {
            val directionVector = (point - acc.previousPoint).normalize()
            val result = Some(acc.previousPoint + directionVector.scale(acc.remainingMeasure))
            AlgorithmState(point, acc.remainingMeasure - distance, result)
          } else {
            AlgorithmState(point, acc.remainingMeasure - distance, None)
          }
        }
      }
      state.result
    }
  }

}

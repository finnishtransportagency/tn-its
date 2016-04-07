package fi.liikennevirasto.digiroad2.tnits.openlr

import java.awt.geom.{Path2D, Point2D}
import java.util
import java.util.Locale

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import openlr.map._

case class DigiroadLine(id: Long, geometry: Seq[Point], length: Int, linkType: Int = 1, functionalClass: Int = 1) extends Line {
  override def getGeoCoordinateAlongLine(distanceAlong: Int): GeoCoordinates =
    DigiroadCoordinates(calculatePointFromLinearReference(geometry, distanceAlong.toDouble).get)

  override def getID: Long =
    id

  override def getLineLength: Int =
    length

  override def getFRC: FunctionalRoadClass =
   functionalClass match {
     case 1 => FunctionalRoadClass.FRC_0
     case 2 => FunctionalRoadClass.FRC_1
     case 3 => FunctionalRoadClass.FRC_2
     case 4 => FunctionalRoadClass.FRC_3
     case 5 => FunctionalRoadClass.FRC_4
     case 6 => FunctionalRoadClass.FRC_5
     case 7 => FunctionalRoadClass.FRC_6
     case 8 => FunctionalRoadClass.FRC_7
   }

  override def getStartNode: Node =
    DigiroadNode(DigiroadCoordinates(geometry(0)))

  override def getEndNode: Node =
    DigiroadNode(DigiroadCoordinates(geometry.last))

  override def getFOW: FormOfWay =
    linkType match {
      case 1 => FormOfWay.MOTORWAY
      case 2 => FormOfWay.MULTIPLE_CARRIAGEWAY
      case 3 => FormOfWay.SINGLE_CARRIAGEWAY
      case 4 => FormOfWay.OTHER
      case 5 => FormOfWay.ROUNDABOUT
      case 6 => FormOfWay.SLIPROAD
      case 7 => FormOfWay.OTHER
      case 8 => FormOfWay.OTHER
      case 9 => FormOfWay.OTHER
      case 10 => FormOfWay.OTHER
      case 11 => FormOfWay.OTHER
      case 12 => FormOfWay.OTHER
      case 13 => FormOfWay.OTHER
      case 21 => FormOfWay.OTHER
    }

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

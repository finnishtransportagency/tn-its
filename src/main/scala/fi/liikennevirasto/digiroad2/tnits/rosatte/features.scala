package fi.liikennevirasto.digiroad2.tnits.rosatte

import fi.liikennevirasto.digiroad2.tnits.geojson
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.RoadLink

case class ValidityPeriodDayOfWeek(value: Int)
case class ValidityPeriod(startHour: Int, endHour: Int, days: Int, startMinute: Int = 0, endMinute: Int = 0)
case class ProhibitionValue(typeId: Int, validityPeriod: Set[ValidityPeriod], exceptions: Set[Int], additionalInfo: String = "")

trait AssetProperties {
  val sideCode: Int
  val changeType: String
  val value: Any
  val startMeasure: Double
  val endMeasure: Double
  val link: RoadLink

  def setSideCode(sideCode: Int): AssetProperties
}

//case class LinearAssetProperties (sideCode: Int, changeType: String, value: Any, startMeasure: Double, endMeasure: Double, link: RoadLink) extends AssetProperties {
//  override def setSideCode(sideCode: Int): AssetProperties = copy(sideCode = sideCode)
//}
//
//case class PointAssetProperties (sideCode: Int, changeType: String, value: Any, startMeasure: Double, endMeasure: Double, link: RoadLink) extends AssetProperties {
//  override def setSideCode(sideCode: Int): AssetProperties = copy(sideCode = sideCode)
//}
//
//sealed trait Value {
//  def toJson: Any
//}
//case class NumericValue(value: Int) extends Value {
//  override def toJson: Any = value
//}
//case class TextualValue(value: String) extends Value {
//  override def toJson: Any = value
//}

/** GeoJSON types specialized to OTH assets. */
object features {
  type Asset = geojson.Feature[AssetProperties]
  type RoadLink = geojson.FeatureLinear[RoadLinkProperties]

  case class NumericAssetProperties(
    sideCode: Int,
    changeType: String,
    value: Int,
    startMeasure: Double,
    endMeasure: Double,
    link: RoadLink) extends  AssetProperties {
    override def setSideCode(sideCode: Int): AssetProperties = copy(sideCode = sideCode)
  }

  case class VehicleProhibitionAssetProperties(
      sideCode: Int,
      changeType: String,
      value: Seq[ProhibitionValue],
      startMeasure: Double,
      endMeasure: Double,
      link: RoadLink) extends  AssetProperties {
    override def setSideCode(sideCode: Int): AssetProperties = copy(sideCode = sideCode)
  }

  case class PointAssetProperties(
      sideCode: Int,
      changeType: String,
      value: String,
      startMeasure: Double,
      endMeasure: Double,
      link: RoadLink) extends  AssetProperties {
    override def setSideCode(sideCode: Int): AssetProperties = copy(sideCode = sideCode)
  }

  case class RoadLinkProperties(
    functionalClass: Int,
    `type`: Int,
    length: Double)

  case class ProhibitionTypesOperations(typeId: Int, exceptions: Set[Int]) {
    val mapVehicleType = Map((3, Seq("AllVehicle"))
      ,(2	, Seq("AllVehicle"))
      ,(12 , Seq("Pedestrian"))
      ,(11 , Seq("Bicycle"))
      ,(10 , Seq("Moped"))
      ,(9	, Seq("Motorcycle"))
      ,(5	, Seq("PublicBus", "PrivateBus"))
      ,(8	, Seq("Taxi"))
      ,(7 , Seq("PassangerCar"))
      ,(6	, Seq("DeliveryTruck"))
      ,(4	, Seq("TransportTruck"))
      ,(19 , Seq("MilitaryVehicle"))
      ,(13 , Seq("CarWithTrailer"))
      ,(14 , Seq("FarmVehicle"))
      ,(21 , Seq("DeliveryTruck", "EmergencyVehicle", "FacilityVehicle", "MailVehicle"))
      ,(22 , Seq("ResidentialVehicle")))

    def vehicleConditionExceptions(): Set[String] = {
      val excludedType = Set(23, 26, 27, 15)

      exceptions.diff(excludedType).flatMap { exception =>
        mapVehicleType(exception)
      }
    }

    def vehicleConditionType(): Set[String] = {
      val excludedType = Set(23, 26, 27, 15, 21, 22)
      if(!excludedType.contains(typeId))
        mapVehicleType(typeId).toSet

      else Set.empty
    }
  }

  case class ValidityPeriodOperations(startHour: Int, endHour: Int, days: Int, startMinute: Int, endMinute: Int) {
    def fromTimeDomainValue() : (Int, Int)  =
      days match {
      case 1 => (6, 1)
      case 2 => (1, 5)
      case 7 => (7, 1)
    }

    def duration(): Int = {
      val startTotalMinutes = startMinute + (startHour * 60)
      val endTotalMinutes = endMinute + (endHour * 60)

      if (endTotalMinutes > startTotalMinutes) {
        val duration = endTotalMinutes - startTotalMinutes
        duration * 60
      } else {
        val duration = 1440 - startTotalMinutes + endTotalMinutes
        duration * 60
      }
    }
  }
}

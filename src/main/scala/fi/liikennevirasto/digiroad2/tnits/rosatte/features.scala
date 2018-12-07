package fi.liikennevirasto.digiroad2.tnits.rosatte

import fi.liikennevirasto.digiroad2.tnits.geojson
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.RoadLink

case class ValidityPeriodDayOfWeek(value: Int)
case class ValidityPeriod(startHour: Int, endHour: Int, days: Int, startMinute: Int = 0, endMinute: Int = 0)
case class ProhibitionValue(typeId: Int, validityPeriod: Set[ValidityPeriod], exceptions: Set[Int], additionalInfo: String = "")

trait AssetProperties {
  val sideCode: Int
  val changeType: String
  val link: RoadLink

  def setSideCode(sideCode: Int): AssetProperties
}

trait LinearAssetProperties extends AssetProperties {
  val sideCode: Int
  val changeType: String
  val value: Any
  val startMeasure: Double
  val endMeasure: Double
  val link: RoadLink

  def setSideCode(sideCode: Int): AssetProperties
}

trait PointAssetProperties extends AssetProperties {
  val sideCode: Int
  val changeType: String
  val mValue: Double
  val link: RoadLink

  def setSideCode(sideCode: Int): AssetProperties
}

/** GeoJSON types specialized to OTH assets. */
object features {
  type Asset = geojson.Feature[AssetProperties]
  type RoadLink = geojson.FeatureLinear[RoadLinkProperties]

  case class LinearNumericAssetProperties(
                                           sideCode: Int,
                                           changeType: String,
                                           value: Int,
                                           startMeasure: Double,
                                           endMeasure: Double,
                                           link: RoadLink) extends LinearAssetProperties {
    override def setSideCode(sideCode: Int): LinearAssetProperties = copy(sideCode = sideCode)
  }

  case class VehicleProhibitionProperties(
                                           sideCode: Int,
                                           changeType: String,
                                           value: Seq[ProhibitionValue],
                                           startMeasure: Double,
                                           endMeasure: Double,
                                           link: RoadLink) extends LinearAssetProperties {
    override def setSideCode(sideCode: Int): LinearAssetProperties = copy(sideCode = sideCode)
  }

  case class IncomingPointAssetProperties(
                                           sideCode: Int,
                                           changeType: String,
                                           typeValue: Option[Int],
                                           mValue: Double,
                                           link: RoadLink) extends PointAssetProperties {
    override def setSideCode(sideCode: Int): PointAssetProperties = copy(sideCode = sideCode)
  }

  case class TrafficSigns(typeId: Int) {
    def warningSign: String = {
      val warningSignType = Map(
        9 -> "Danger",
        36 -> "DangerousCurve",
        37 -> "DangerousCurve",
        38 -> "DangerousCurve",
        39 -> "DangerousCurve",
        40 -> "Slope",
        41 -> "Slope",
        42 -> "UnevenRoadSurface",
        43 -> "ChildrenPlaying",
        82 -> "NarrowingRoad",
        83 -> "TwoWayTraffic",
        84 -> "MovingBridge",
        85 -> "ConstructionWork",
        86 -> "SlipperyRoad",
        87 -> "PedestriansCrossing",
        88 -> "CyclistsCrossing",
        89 -> "DangerousIntersection",
        90 -> "TrafficLightsAhead",
        91 -> "RailwayCrossing",
        92 -> "Rockfall",
        93 -> "SideWind",
        125 -> "Moose",
        126 -> "Reindeer"
      )
      warningSignType(typeId)
    }
  }

  case class RoadLinkProperties(
                                 functionalClass: Int,
                                 `type`: Int,
                                 length: Double)

  case class ProhibitionTypesOperations(typeId: Int, exceptions: Set[Int]) {
    val mapVehicleType: Map[Int, Seq[String]] = Map(
      3 -> Seq("AllVehicle"),
      2	-> Seq("AllVehicle"),
      12 -> Seq("Pedestrian"),
      11 -> Seq("Bicycle"),
      10 -> Seq("Moped"),
      9	-> Seq("Motorcycle"),
      5	-> Seq("PublicBus", "PrivateBus"),
      8	-> Seq("Taxi"),
      7 -> Seq("PassangerCar"),
      6	-> Seq("DeliveryTruck"),
      4	-> Seq("TransportTruck"),
      19 -> Seq("MilitaryVehicle"),
      13 -> Seq("CarWithTrailer"),
      14 -> Seq("FarmVehicle"),
      21 -> Seq("DeliveryTruck", "EmergencyVehicle", "FacilityVehicle", "MailVehicle"),
      22 -> Seq("ResidentialVehicle"))


    def vehicleConditionExceptions(): Set[String] = {
      val excludedType = Set(23, 26, 27, 28, 15)

      exceptions.diff(excludedType).flatMap { exception =>
        mapVehicleType(exception)
      }
    }

    def vehicleConditionType(): Set[String] = {
      val excludedType = Set(23, 26, 27, 28, 15, 21, 22)
      if(!excludedType.contains(typeId))
        mapVehicleType(typeId).toSet

      else Set.empty
    }
  }

  case class ValidityPeriodOperations(startHour: Int, endHour: Int, days: Int, startMinute: Int, endMinute: Int) {
    def fromTimeDomainValue() : (Int, Int)  =
      days match {
        case 1 => (1, 5) //Weekday
        case 2 => (6, 1) //Saturday
        case 3 => (7, 1) //Sunday
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

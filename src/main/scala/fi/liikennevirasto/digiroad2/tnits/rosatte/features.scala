package fi.liikennevirasto.digiroad2.tnits.rosatte

import fi.liikennevirasto.digiroad2.tnits.geojson

/** GeoJSON types specialized to OTH assets. */
object features {
  type Asset = geojson.Feature[AssetProperties]
  type RoadLink = geojson.Feature[RoadLinkProperties]

  case class AssetProperties(
    sideCode: Int,
    changeType: String,
    value: Any,
    startMeasure: Double,
    endMeasure: Double,
    link: RoadLink)

  case class RoadLinkProperties(
    functionalClass: Int,
    `type`: Int,
    length: Double)

  case class ProhibitionValue (typeId: Int, validityPeriods: Seq[ValidityPeriods], exceptions: Set[Int]) {
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

      exceptions.intersect(excludedType).flatMap { exception =>
        mapVehicleType(exception)
      }
    }

    def vehicleConditionType(): Set[String] = {
      val excludedType = Set(23, 26, 27, 15, 21, 22)

      exceptions.intersect(excludedType).flatMap { exception =>
        mapVehicleType(exception)
      }
    }
  }

  case class ValidityPeriods(startHour: Int, endHour: Int, days: Int, startMinute: Int, endMinute: Int) {
    def fromTimeDomainValue() : (Int, Int)  =
      days match {
      case 1 => (6, 1)
      case 2 => (1, 5)
      case 7 => (7, 1)
    }

    def duration(): Int = {
      val startHourAndMinutes: Double = (startMinute / 60.0) + startHour
      val endHourAndMinutes: Double = (endMinute / 60.0) + endHour

      if (endHourAndMinutes > startHourAndMinutes) {
        Math.ceil(endHourAndMinutes - startHourAndMinutes).toInt
      } else {
        Math.ceil(24 - startHourAndMinutes + endHourAndMinutes).toInt
      }
    }
  }
}

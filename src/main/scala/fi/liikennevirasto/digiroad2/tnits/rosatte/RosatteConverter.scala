package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.{BufferedOutputStream, OutputStream, OutputStreamWriter}
import java.time.Instant
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson.Feature
import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.openlr.OpenLREncoder
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.{ProhibitionTypesOperations, ValidityPeriodOperations}
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

import scala.util.{Failure, Success, Try}

/** Generates a dataset. */
object RosatteConverter {


  /** Converts the given [[fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset]]s
    * to Rosatte XML and writes it to the provided stream. */
  def convertDataSet(featureMembers: Seq[(AssetType, Seq[Feature[AssetProperties]])], start: Instant, end: Instant, dataSetId: String, output: OutputStream): Unit = {
    generateChangeData(featureMembers, dataSetId, start, end, output)
  }

  private def generateChangeData(featureMembers: Seq[(AssetType, Seq[Feature[AssetProperties]])], dataSetId: String, startTime: Instant, endTime: Instant, output: OutputStream): Unit = {
    val writer = new OutputStreamWriter(new BufferedOutputStream(output), "UTF-8")
    writer.write(
      s"""<rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink"
            xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd"
            xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2"
            xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst"
            xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd"
            gml:id="${UUID.randomUUID().toString}">""")

    featureMembers.foreach { case (assetType, changes) =>
      // We need to split two-way features into two one-way features because of OpenLR encoding
      val onlyOneWayFeatures = splitFeaturesApplicableToBothDirections(changes)
      onlyOneWayFeatures.foreach { feature =>
        val featureMember = toFeatureMember(feature, assetType.featureType, assetType.valueType, assetType.unit, assetType.apiEndPoint)
        writer.write(featureMember.toString)
      }
    }

    writer.write(
      s"""<rst:metadata>
            <rst:datasetId>$dataSetId</rst:datasetId>
            <rst:datasetCreationTime>$endTime</rst:datasetCreationTime>
          </rst:metadata>
          <rst:type>Update</rst:type>
        </rst:ROSATTESafetyFeatureDataset>""")

    writer.flush()
  }

  private def splitFeaturesApplicableToBothDirections(assets: Seq[Feature[AssetProperties]]): Seq[Feature[AssetProperties]] = {
    assets.flatMap { feature =>
      feature.properties.sideCode match {
        case 1 =>
          Seq(feature.copy(properties = feature.properties.setSideCode(sideCode = 2)),
            feature.copy(properties = feature.properties.setSideCode(sideCode = 3)))
        case _ =>
          Seq(feature)
      }
    }
  }

  private def toFeatureMember(feature: Feature[AssetProperties], featureType: String, valueType: String, unit: String, assetType: String) = {
    val coordinates = feature.geometry.coordinates.flatMap(_.take(2))
    val transformedCoordinates = CoordinateTransform.convertToWgs84(coordinates)
    val geometry = transformedCoordinates.mkString(" ")
    val startMeasure = feature.properties.startMeasure
    val endMeasure = feature.properties.endMeasure
    val link = feature.properties.link
    val linkReference = "FI.1000018." + link.id
    val applicableDirection = feature.properties.sideCode match {
      case 2 => "inDirection"
      case 3 => "inOppositeDirection"
      case _ => ""
    }

    val properties = assetType match {

      case "vehicle_prohibitions" =>
        feature.properties.value.asInstanceOf[Seq[ProhibitionValue]].map { prohibitionValue =>
          <rst:condition>
            <rst:ConditionSet>
              <rst:conditions>
                <rst:VehicleCondition>
                  <rst:negate>false</rst:negate>
                  <rst:vehicleType>
                    {ProhibitionTypesOperations(prohibitionValue.typeId, prohibitionValue.exceptions).vehicleConditionType()}
                  </rst:vehicleType>
                </rst:VehicleCondition>
                <rst:VehicleCondition>
                  <rst:negate>true</rst:negate>
                  {ProhibitionTypesOperations(prohibitionValue.typeId, prohibitionValue.exceptions).vehicleConditionExceptions().map{ exception => <rst:vehicleType> {exception} </rst:vehicleType> }}
                </rst:VehicleCondition>
              </rst:conditions>
              <rst:operator>AND</rst:operator>
            </rst:ConditionSet>
            <rst:ConditionSet>
              <rst:conditions>
                <rst:TimeCondition>
                  {prohibitionValue.validityPeriod.map { validityPeriod =>
                  <rst:validityPeriod>
                    <rst:ValidityPeriod>
                      <rst:time>
                        <rst:weekday>
                          <rst:IntegerInterval>
                            <rst:start>
                              {ValidityPeriodOperations(validityPeriod.startHour, validityPeriod.endHour, validityPeriod.days, validityPeriod.startMinute, validityPeriod.endMinute).fromTimeDomainValue()._1}
                            </rst:start>
                            <rst:length>
                              {ValidityPeriodOperations(validityPeriod.startHour, validityPeriod.endHour, validityPeriod.days, validityPeriod.startMinute, validityPeriod.endMinute).fromTimeDomainValue()._2}
                            </rst:length>
                          </rst:IntegerInterval>
                        </rst:weekday>
                        <rst:begin>
                          {s"${validityPeriod.startHour}:${validityPeriod.startMinute}:00"}
                        </rst:begin>
                        <rst:lengthSeconds>
                          {ValidityPeriodOperations(validityPeriod.startHour, validityPeriod.endHour, validityPeriod.days, validityPeriod.startMinute, validityPeriod.endMinute).duration()}
                        </rst:lengthSeconds>
                      </rst:time>
                    </rst:ValidityPeriod>
                  </rst:validityPeriod>
                }}
                </rst:TimeCondition>
              </rst:conditions>
              <rst:operator>OR</rst:operator>
            </rst:ConditionSet>
          </rst:condition>
        }
      case _ =>
        <rst:properties>
          <rst:SafetyFeaturePropertyValue>
            <rst:type>{valueType}</rst:type>
            <rst:propertyValue>
              <gml:measure uom={unit}>{feature.properties.value}</gml:measure>
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
    }

    val openLR = encodeOpenLRLocationString(startMeasure, endMeasure, applicableDirection, link, linkReference)
    openLR match {
      case Failure(reason) =>
        println(reason)
      case Success(reference) =>
        <gml:featureMember>
          <rst:GenericSafetyFeature gml:id={ UUID.randomUUID().toString }>
            <rst:id>
              <rst:SafetyFeatureId>
                <rst:providerId>FI.LiVi.OTH</rst:providerId>
                <rst:id>{feature.id}</rst:id>
              </rst:SafetyFeatureId>
            </rst:id>
            <rst:locationReference>
              <rst:INSPIRELinearLocation gml:id={ UUID.randomUUID().toString }>
                <net:SimpleLinearReference>
                  <net:element xlink:href={ linkReference }/>
                  <net:applicableDirection>{ applicableDirection }</net:applicableDirection>
                  <net:fromPosition uom="meter">{ startMeasure }</net:fromPosition>
                  <net:toPosition uom="meter">{ endMeasure }</net:toPosition>
                </net:SimpleLinearReference>
              </rst:INSPIRELinearLocation>
            </rst:locationReference>
            <rst:locationReference>
              <rst:OpenLRLocationString gml:id={ UUID.randomUUID().toString }>
                <rst:base64String>{ reference }</rst:base64String>
                <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
              </rst:OpenLRLocationString>
            </rst:locationReference>
            <rst:updateInfo>
              <rst:UpdateInfo>
                <rst:type>{ feature.properties.changeType }</rst:type>
              </rst:UpdateInfo>
            </rst:updateInfo>
            <rst:source>Regulation</rst:source>
            <rst:encodedGeometry>
              <gml:LineString gml:id={ UUID.randomUUID().toString } srsDimension="2">
                <gml:posList>
                  { geometry }
                </gml:posList>
              </gml:LineString>
            </rst:encodedGeometry>
            <rst:type>{ featureType }</rst:type>
            {properties}
          </rst:GenericSafetyFeature>
        </gml:featureMember>
    }
  }

  private def encodeOpenLRLocationString(startMeasure: Double, endMeasure: Double, applicableDirection: String, link: features.RoadLink, linkId: String): Try[String] = {
    val coordinates =
      link.geometry.coordinates.flatten

    val points =
      coordinates
        .grouped(3)
        .map { case Seq(x, y, z) => Point(x, y, z) }
        .toSeq

    val linkLength = link.properties.length
    val functionalClass = link.properties.functionalClass
    val linkType = link.properties.`type`
    val (linkGeometry, startM, endM) =
      if (applicableDirection == "inOppositeDirection")
        (points.reverse, linkLength - endMeasure, linkLength - startMeasure)
      else
        (points, startMeasure, endMeasure)

    OpenLREncoder.encodeAssetOnLink(startM, endM, linkGeometry, linkLength, functionalClass, linkType, linkId)
  }
}

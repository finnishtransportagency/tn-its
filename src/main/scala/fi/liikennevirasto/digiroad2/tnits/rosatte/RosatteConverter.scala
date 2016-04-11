package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.time.Instant
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.openlr.OpenLREncoder

object RosatteConverter {
  def convert(speedLimits: Seq[features.Asset], start: Instant, end: Instant): DataSet = {
    val onlyOneWaySpeedLimits = splitFeaturesApplicableToBothDirections(speedLimits)
    val rosatteData = convertToChangeDataSet(onlyOneWaySpeedLimits, start, end)
    val dataSetId = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
    DataSet(
      id = dataSetId,
      updates = rosatteData.toString)
  }

  def convertToChangeDataSet(speedLimitFeatures: Seq[features.Asset], startTime: Instant, endTime: Instant): Any = {
    // todo: replace current time with the latest modification time from changes
    //       or specify explicitly in the change api
    <rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2" xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd" gml:id="i0fbf03ad-5c7a-4490-bb7c-64f95a91cb3c">
      {speedLimitFeatures.map(featureMember)}<rst:metadata>
      <rst:datasetId>
        {DatasetID.encode(DatasetID.LiikennevirastoUUID, startTime, endTime)}
      </rst:datasetId>
      <rst:datasetCreationTime>
        {endTime}
      </rst:datasetCreationTime>
    </rst:metadata>
      <rst:type>Update</rst:type>
    </rst:ROSATTESafetyFeatureDataset>
  }

  def splitFeaturesApplicableToBothDirections(assets: Seq[features.Asset]): Seq[features.Asset] = {
    assets.flatMap { feature =>
      feature.properties.sideCode match {
        case 1 =>
          Seq(feature.copy(properties = feature.properties.copy(sideCode = 2)),
            feature.copy(properties = feature.properties.copy(sideCode = 3)))
        case _ =>
          Seq(feature)
      }
    }
  }

  def featureMember(feature: features.Asset) = {
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
    val openLR = encodeOpenLRLocationString(startMeasure, endMeasure, applicableDirection, link)
    <gml:featureMember>
      <rst:GenericSafetyFeature gml:id={UUID.randomUUID().toString}>
        <rst:id>
          <rst:SafetyFeatureId>
            <rst:providerId>FI.LiVi.OTH</rst:providerId>
            <rst:id>
              {feature.id}
            </rst:id>
          </rst:SafetyFeatureId>
        </rst:id>
        <rst:locationReference>
          <rst:INSPIRELinearLocation gml:id={UUID.randomUUID().toString}>
            <net:SimpleLinearReference>
              <net:element xlink:href={linkReference}/>
              <net:applicableDirection>
                {applicableDirection}
              </net:applicableDirection>
              <net:fromPosition uom="meter">
                {startMeasure}
              </net:fromPosition>
              <net:toPosition uom="meter">
                {endMeasure}
              </net:toPosition>
            </net:SimpleLinearReference>
          </rst:INSPIRELinearLocation>
        </rst:locationReference>
        <rst:locationReference>
          <rst:OpenLRLocationString gml:id={UUID.randomUUID().toString}>
            <rst:base64String>
              {openLR}
            </rst:base64String>
            <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
          </rst:OpenLRLocationString>
        </rst:locationReference>
        <rst:updateInfo>
          <rst:UpdateInfo>
            <rst:type>
              {feature.properties.changeType}
            </rst:type>
          </rst:UpdateInfo>
        </rst:updateInfo>
        <rst:source>Regulation</rst:source>
        <rst:encodedGeometry>
          <gml:LineString gml:id={UUID.randomUUID().toString} srsDimension="2">
            <gml:posList>
              {geometry}
            </gml:posList>
          </gml:LineString>
        </rst:encodedGeometry>
        <rst:type>SpeedLimit</rst:type>
        <rst:properties>
          <rst:SafetyFeaturePropertyValue>
            <rst:type>MaximumSpeedLimit</rst:type>
            <rst:propertyValue>
              <gml:measure uom="kmph">
                {feature.properties.value}
              </gml:measure>
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
      </rst:GenericSafetyFeature>
    </gml:featureMember>
  }

  private def encodeOpenLRLocationString(startMeasure: Double, endMeasure: Double, applicableDirection: String, link: features.RoadLink): String = {
    val coordinates =
      link.geometry.coordinates.flatten

    val points =
      coordinates
        .grouped(3)
        .map { case Seq(x, y, z) => Point(x, y, z) }
        .toSeq

    val linkGeometry =
      if (applicableDirection == "inOppositeDirection")
        points.reverse
      else
        points

    val linkLength = link.properties.length
    val functionalClass = link.properties.functionalClass
    val linkType = link.properties.`type`

    OpenLREncoder.encodeAssetOnLink(startMeasure, endMeasure, linkGeometry, linkLength, functionalClass, linkType)
  }
}

package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson.FeaturePoint
import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.openlr.OpenLREncoder
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.{IncomingPointAssetProperties, TrafficSigns}
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

import scala.util.Try
import scala.xml.{Elem, NodeBuffer, NodeSeq}

/** Generates a dataset. */
class PointRosatteConverter extends AssetRosatteConverter {
  override type AssetPropertiesType = PointAssetProperties
  override type FeatureType = FeaturePoint[AssetPropertiesType]

  override def locationReference(feature: FeaturePoint[PointAssetProperties], reference: String): NodeBuffer = {
    val properties = feature.properties

    <rst:locationReference>
      <rst:INSPIRELinearLocation gml:id={UUID.randomUUID().toString}>
        <net:SimpleLinearReference>
          <net:element xlink:href={DefaultLinkReference + properties.link.id}/>
          {applicableDirection(properties.sideCode)}
          <net:atPosition uom="meter">
            {properties.mValue}
          </net:atPosition>
        </net:SimpleLinearReference>
      </rst:INSPIRELinearLocation>
    </rst:locationReference>
      <rst:locationReference>
        <rst:OpenLRLocationString gml:id={UUID.randomUUID().toString}>
          <rst:base64String>
            {reference}
          </rst:base64String>
          <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
        </rst:OpenLRLocationString>
      </rst:locationReference>

  }

  override def geometry(feature: FeaturePoint[PointAssetProperties]): String = {
    val coordinates = Seq(feature.geometry.coordinates).flatMap(_.take(2))
    val transformedCoordinates = CoordinateTransform.convertToWgs84(coordinates)
    transformedCoordinates.mkString(" ")
  }

  override def properties(assetType: AssetType, feature: FeaturePoint[PointAssetProperties]) : NodeSeq  = {
    NodeSeq.Empty
  }

  override def encodeOpenLRLocationString(feature: FeaturePoint[PointAssetProperties]): Try[String] = {
    val properties = feature.properties
    val link = properties.link

    val coordinates = link.geometry.coordinates.flatten

    val points = coordinates
      .grouped(3)
      .map { case Seq(x, y, z) => Point(x, y, z) }
      .toSeq

    val linkLength = link.properties.length
    val functionalClass = link.properties.functionalClass
    val linkType = link.properties.`type`

    OpenLREncoder.encodeAssetOnLink(properties.mValue, properties.mValue, points, linkLength, functionalClass, linkType,  DefaultLinkReference + link.id)
  }

  override def splitFeaturesApplicableToBothDirections(assets: Seq[FeaturePoint[PointAssetProperties]], assetType: AssetType): Seq[FeaturePoint[PointAssetProperties]] = {
    assets
  }

  override def applicableDirection(sideCode: Int): Elem = {
    def buildDirection(direction: String): Elem = {
      <net:applicableDirection>
        {direction}
      </net:applicableDirection>
    }

    sideCode match {
      case 1 => buildDirection("bothDirection")
      case 2 => buildDirection("inDirection")
      case 3 => buildDirection("inOppositeDirection")
      case 99 => <net:applicableDirection xsi:nil="true" nilReason="http://inspire.ec.europa.eu/codelist/VoidReasonValue/Unpopulated "/>
      case _ => throw new IllegalArgumentException(s"Applicable direction value $sideCode not supported")
    }
  }
}
class PointValueRosatteConverter extends PointRosatteConverter {

  override def properties(assetType: AssetType, feature: FeaturePoint[PointAssetProperties]): NodeSeq = {
    assetType.apiEndPoint match {
      case "warning_signs_group" =>
        <rst:properties>
          <rst:SafetyFeaturePropertyValue>
            <rst:type>
              {assetType.valueType}
            </rst:type>
            <rst:propertyValue>
              {TrafficSigns(feature.properties.asInstanceOf[IncomingPointAssetProperties].typeValue.get).warningSign}
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
      case _ =>
        NodeSeq.Empty
    }

  }
}

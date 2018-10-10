package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson.FeaturePoint
import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.openlr.OpenLREncoder
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

import scala.util.Try
import scala.xml.{NodeBuffer, NodeSeq}

/** Generates a dataset. */
object PointRosatteConverter extends AssetRosatteConverter {
  override type AssetPropertiesType = PointAssetProperties
  override type FeatureType = FeaturePoint[AssetPropertiesType]

  override def locationReference(feature: FeaturePoint[PointAssetProperties], reference: String) : NodeBuffer   = {
    val properties = feature.properties

      <rst:locationReference>
      <rst:INSPIRELinearLocation gml:id= { UUID.randomUUID().toString }>
        <net:SimpleLinearReference>
          <net:element xlink:href= { DefaultLinkReference + properties.link.id }/>
          <net:applicableDirection> { applicableDirection(properties.sideCode) }</net:applicableDirection>
          <net:atPosition uom="meter"> { properties.endMeasure }</net:atPosition>
        </net:SimpleLinearReference>
      </rst:INSPIRELinearLocation>
    </rst:locationReference>
    <rst:locationReference>
       <rst:OpenLRLocationString gml:id= { UUID.randomUUID().toString }>
         <rst:base64String> { reference }</rst:base64String>
         <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
       </rst:OpenLRLocationString>
    </rst:locationReference>

  }

  override def geometry(feature: FeaturePoint[PointAssetProperties] ) : String = {
    val coordinates = Seq(feature.geometry.coordinates).flatMap(_.take(2))
    val transformedCoordinates = CoordinateTransform.convertToWgs84(coordinates)
    transformedCoordinates.mkString(" ")
  }

  override def properties(assetType: AssetType, feature: FeaturePoint[PointAssetProperties]) : NodeSeq  = {
    <rst:properties>
      <rst:SafetyFeaturePropertyValue>
        <rst:type>
          {assetType.valueType}
        </rst:type>
      </rst:SafetyFeaturePropertyValue>
    </rst:properties>
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

    OpenLREncoder.encodeAssetOnLink(properties.endMeasure, properties.endMeasure, points, linkLength, functionalClass, linkType,  DefaultLinkReference + link.id)
  }

  override def splitFeaturesApplicableToBothDirections(assets: Seq[FeaturePoint[PointAssetProperties]], assetType : AssetType): Seq[FeaturePoint[PointAssetProperties]] = {
    assets
  }

  override def applicableDirection(sideCode: Int) : String = {
    sideCode match  {
      case 1 => "bothDirection"
      case 2 => "inDirection"
      case 3 => "inOppositeDirection"
      case _ => ""
    }
  }

}

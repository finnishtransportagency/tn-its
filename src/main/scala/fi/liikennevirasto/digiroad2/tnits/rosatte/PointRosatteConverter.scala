package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.OutputStreamWriter
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson.{Feature, FeatureLinear, FeaturePoint}
import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.openlr.OpenLREncoder
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

import scala.util.Try
import scala.xml.{Elem, NodeBuffer, NodeSeq}

/** Generates a dataset. */
object PointRosatteConverter extends AssetRosatteConverter {
  type FeatureType = FeaturePoint[AssetProperties]
//  type AssetPropertiesType = PointAssetProperties

  override def applicableDirection(sideCode: Int) : String = {
    sideCode match {
      case 1 => "bothDirection"
      case 2 => "inDirection"
      case 3 => "inOppositeDirection"
      case _ => ""
    }
  }

  override def locationReference(feature: FeaturePoint[AssetProperties], reference: String) : NodeBuffer   = {
    val properties = feature.properties

      <rst:locationReference>
      <rst:INSPIRELinearLocation gml:id= { UUID.randomUUID().toString }>
        <net:SimpleLinearReference>
          <net:element xlink:href= { DefaultLinkReference + properties.link.id }/>
          <net:applicableDirection> { applicableDirection(properties.sideCode) }</net:applicableDirection>
          <net:atPosition uom="meter"> { properties.startMeasure }</net:atPosition>
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

  override def geometry(feature: FeaturePoint[AssetProperties] ) : String = {
    val coordinates = Seq(feature.geometry.coordinates).flatMap(_.take(2))
    val transformedCoordinates = CoordinateTransform.convertToWgs84(coordinates)
    transformedCoordinates.mkString(" ")
  }

  override def properties(assetType: AssetType, feature: FeaturePoint[AssetProperties]) : NodeSeq  = {
    <rst:properties>
      <rst:SafetyFeaturePropertyValue>
        <rst:type>
          {assetType.valueType}
        </rst:type>
      </rst:SafetyFeaturePropertyValue>
    </rst:properties>
  }

  override def encodeOpenLRLocationString(feature: FeaturePoint[AssetProperties]): Try[String] = {
    val properties = feature.properties
    val link = properties.link

    val coordinates = link.geometry.coordinates.flatten

    val points =
      coordinates
        .grouped(3)
        .map { case Seq(x, y, z) => Point(x, y, z) }
        .toSeq

    val linkLength = link.properties.length
    val functionalClass = link.properties.functionalClass
    val linkType = link.properties.`type`
    val (linkGeometry, startM) =
      if (properties.sideCode == 3) //inOppositeDirection
        (points.reverse, linkLength - properties.startMeasure)
      else
        (points, properties.startMeasure)

    OpenLREncoder.encodeAssetOnLink(startM, startM, linkGeometry, linkLength, functionalClass, linkType, DefaultLinkReference + link.id)
  }

  override def duplicateFeature(feature: FeaturePoint[AssetProperties]) : Seq[FeaturePoint[AssetProperties]] = {
    Seq(feature.copy(properties = feature.properties.setSideCode(sideCode = 2)),
      feature.copy(properties = feature.properties.setSideCode(sideCode = 3)))
  }

}

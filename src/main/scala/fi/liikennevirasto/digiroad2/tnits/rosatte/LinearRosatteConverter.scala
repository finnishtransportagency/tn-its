package fi.liikennevirasto.digiroad2.tnits.rosatte


import java.io.OutputStreamWriter
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson.{Feature, FeatureLinear}
import fi.liikennevirasto.digiroad2.tnits.geometry.{CoordinateTransform, Point}
import fi.liikennevirasto.digiroad2.tnits.openlr.OpenLREncoder
import fi.liikennevirasto.digiroad2.tnits.oth.OTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.{BogieWeightLimitAssetProperties, LinearNumericAssetProperties, ProhibitionTypesOperations, ValidityPeriodOperations}
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeBuffer, NodeSeq}

/** Generates a dataset. */


class LinearRosatteConverter extends AssetRosatteConverter {
  override type AssetPropertiesType = LinearAssetProperties
  override type FeatureType = FeatureLinear[AssetPropertiesType]


  override def locationReference(feature: FeatureLinear[LinearAssetProperties], reference: String) : NodeBuffer   = {
    val properties = feature.properties
    <rst:locationReference>
    <rst:INSPIRELinearLocation gml:id={ UUID.randomUUID().toString }>
        <net:SimpleLinearReference>
          <net:element xlink:href= { DefaultLinkReference + properties.link.id } />
          { applicableDirection(properties.sideCode) }
          <net:fromPosition uom="meter">{ properties.startMeasure }</net:fromPosition>
          <net:toPosition uom="meter">{ properties.endMeasure }</net:toPosition>
        </net:SimpleLinearReference>
      </rst:INSPIRELinearLocation>
    </rst:locationReference>
      <rst:locationReference>
        <rst:OpenLRLocationString gml:id= {UUID.randomUUID().toString }>
          <rst:base64String>{ reference }</rst:base64String>
          <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
        </rst:OpenLRLocationString>
      </rst:locationReference>
  }

  def encodeOpenLRLocationString(feature: FeatureLinear[LinearAssetProperties]): Try[String] = {
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
    val (linkGeometry, startM, endM) =
      if (properties.sideCode == 3) //inOppositeDirection
        (points.reverse, linkLength - properties.endMeasure, linkLength - properties.startMeasure)
      else
        (points, properties.startMeasure, properties.endMeasure)

    OpenLREncoder.encodeAssetOnLink(startM, endM, linkGeometry, linkLength, functionalClass, linkType,  DefaultLinkReference + link.id)
  }

  override def splitFeatureMember(assetType: AssetType, changes: Seq[Feature[AssetProperties]], writer: OutputStreamWriter) = {
    assetType.service.splitFeaturesApplicableToBothDirections(changes.asInstanceOf[Seq[assetType.service.FeatureType]], assetType)
      .foreach { feature =>
        val featureMember = assetType.service.toFeatureMember(feature, assetType, writer)
        writer.write(featureMember.toString)
    }
  }

  override def properties(assetType: AssetType, feature: FeatureLinear[LinearAssetProperties]) : NodeSeq = {
    assetType.apiEndPoint match {

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
                  <rst:negate>true</rst:negate>{ProhibitionTypesOperations(prohibitionValue.typeId, prohibitionValue.exceptions).vehicleConditionExceptions().map { exception =>
                  <rst:vehicleType>
                  {exception}
                  </rst:vehicleType>
                }}
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
            <rst:type>{ assetType.valueType }</rst:type>
            <rst:propertyValue>
              <gml:measure uom={assetType.unit}>
                {feature.properties.value}
              </gml:measure>
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
    }
  }

  override def geometry(feature: FeatureLinear[LinearAssetProperties] ) : String  = {
    val coordinates = feature.geometry.coordinates.flatMap(_.take(2))
    val transformedCoordinates = CoordinateTransform.convertToWgs84(coordinates)
    transformedCoordinates.mkString(" ")
  }



  override def splitFeaturesApplicableToBothDirections(assets: Seq[FeatureLinear[LinearAssetProperties]], assetType : AssetType): Seq[FeatureLinear[LinearAssetProperties]] = {
    assets.flatMap { feature =>
      feature.properties.sideCode match {
        case 1 =>
          Seq(feature.copy(properties = feature.properties.setSideCode(sideCode = 2)),
              feature.copy(properties = feature.properties.setSideCode(sideCode = 3)))
        case _ =>
          Seq(feature)
      }
    }.asInstanceOf[Seq[FeatureLinear[LinearAssetProperties]]]
  }

  override def applicableDirection(sideCode: Int) : Elem = {
    val direction = sideCode match  {
      case 2 => "inDirection"
      case 3 => "inOppositeDirection"
      case _ => ""
    }
    <net:applicableDirection>{direction}</net:applicableDirection>
  }

  override def encodedGeometry(feature: FeatureLinear[LinearAssetProperties]) : Elem   = {
    <rst:encodedGeometry>
      <gml:LineString  gml:id={UUID.randomUUID().toString} srsDimension="2">
        <gml:posList>
          {geometry(feature)}
        </gml:posList>
      </gml:LineString >
    </rst:encodedGeometry>
  }
}

class BogieWeightLimitRosatteConverter extends LinearRosatteConverter {

  def splitAssetsByAxleValue(assetType: AssetType, changes: Seq[Feature[AssetProperties]]) = {
    changes.flatMap { change =>
      val propertyValues = change.properties.asInstanceOf[BogieWeightLimitAssetProperties].value
      val changes = change.properties.asInstanceOf[BogieWeightLimitAssetProperties]
      val newProperties = features.LinearNumericAssetProperties(changes.sideCode, changes.changeType, 0, changes.startMeasure, changes.endMeasure, changes.link)

      propertyValues.twoAxleValue.map { value =>
        (AssetType(assetType.apiEndPoint, assetType.featureType, "MaximumWeightPerTwoAxesBogie", assetType.unit, OTHClient, new LinearRosatteConverter),
          FeatureLinear(change.id, changes.link.geometry, newProperties.copy(value = value.toInt)))
      } ++ propertyValues.threeAxleValue.map { value =>
        (AssetType(assetType.apiEndPoint, assetType.featureType, "MaximumWeightPerThreeAxesBogie", assetType.unit, OTHClient, new LinearRosatteConverter),
          FeatureLinear(change.id, changes.link.geometry, newProperties.copy(value = value.toInt)))
      }
    }
  }

  override def splitFeatureMember(assetType: AssetType, changes: Seq[Feature[AssetProperties]], writer: OutputStreamWriter) = {
    splitAssetsByAxleValue(assetType, changes).groupBy(_._1)
      .foreach { case (newAssetType, feature) =>
        newAssetType.service.splitFeaturesApplicableToBothDirections(feature.map(_._2).asInstanceOf[Seq[newAssetType.service.FeatureType]], newAssetType)
          .foreach { feature =>
            val featureMember = newAssetType.service.toFeatureMember(feature, newAssetType, writer)
            writer.write(featureMember.toString)
        }
      }
  }
}

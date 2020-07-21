package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.{BufferedOutputStream, OutputStream, OutputStreamWriter}
import java.time.Instant
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson.Feature
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.BogieWeightLimitAssetProperties
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType

import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, NodeBuffer, NodeSeq}

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
            xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst https://tn-its-test.herokuapp.com/old-schemas/Rosatte.xsd"
            gml:id="${UUID.randomUUID().toString}">""")

    featureMembers.foreach { case (assetType, changes) =>
      // We need to split two-way features into two one-way features because of OpenLR encoding
      assetType.service.splitFeatureMember(assetType, changes, writer)
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
}

trait AssetRosatteConverter {
  type AssetPropertiesType <: AssetProperties
  type FeatureType <: Feature[AssetPropertiesType]

  val DefaultLinkReference = "FI.1000018."

  def toFeatureMember(feature: FeatureType, assetType: AssetType, writer: OutputStreamWriter) : Any = {
    val openLR = encodeOpenLRLocationString(feature)
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
             {locationReference(feature, reference)}
            <rst:updateInfo>
              <rst:UpdateInfo>
                <rst:type>{ feature.properties.changeType }</rst:type>
              </rst:UpdateInfo>
            </rst:updateInfo>
            <rst:source>{assetType.source}</rst:source>
            {encodedGeometry(feature)}
            <rst:type>{ assetType.featureType }</rst:type>
            {properties(assetType, feature)}
          </rst:GenericSafetyFeature>
        </gml:featureMember>
    }
  }

  def properties(assetType: AssetType, feature: FeatureType) : NodeSeq

  def locationReference(feature: FeatureType, reference: String ) : NodeBuffer

  def applicableDirection(sideCode: Int) : Elem

  def encodeOpenLRLocationString(feature: FeatureType): Try[String]

  def geometry(feature: FeatureType) : String

  def splitFeaturesApplicableToBothDirections(assets: Seq[FeatureType], assetType : AssetType): Seq[FeatureType]

  def encodedGeometry(feature: FeatureType) : Elem

  def splitFeatureMember(assetType: AssetType, changes: Seq[Feature[AssetProperties]], writer: OutputStreamWriter): Unit = {
    assetType.service.splitFeaturesApplicableToBothDirections(changes.asInstanceOf[Seq[assetType.service.FeatureType]], assetType)
      .foreach { feature =>
        val featureMember = assetType.service.toFeatureMember(feature, assetType, writer)
        writer.write(featureMember.toString)
      }
  }
}

package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.net.URLEncoder
import java.time.Instant
import java.util.{Base64, UUID}

import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits.{RemoteDatasets, geojson}
import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import fi.liikennevirasto.digiroad2.tnits.openlr.{DigiroadFixtureMapDatabase, DigiroadLine}
import openlr.binary.ByteArray
import openlr.encoder.{OpenLREncoder, OpenLREncoderParameter}
import openlr.location.LocationFactory
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Await
import scala.concurrent.duration._

object RosatteConverter {
  protected implicit val jsonFormats: Formats = DefaultFormats

  case class Properties(
     sideCode: Int,
     changeType: String,
     value: Int,
     startMeasure: Double,
     endMeasure: Double,
     id: Long,
     link: Map[String, Any])

  type Feature = geojson.Feature[Properties]

  private val changesApiUrl: Req =
    host(sys.env.getOrElse("CHANGE_API_URL", ""))
      .setFollowRedirects(true)
      .as(
        user = sys.env.getOrElse("CHANGE_API_USERNAME", ""),
        password = sys.env.getOrElse("CHANGE_API_PASSWORD", ""))

  def main(args: Array[String]) {
    try {
      val start = Instant.parse("2016-03-01T22:00:00Z")
      val end = Instant.now
      val speedLimitFeatures = readSpeedLimitChanges(start)
      val onlyOneWaySpeedLimitFeatures = splitFeaturesApplicableToBothDirections(speedLimitFeatures)
      val result = convertToChangeDataSet(onlyOneWaySpeedLimitFeatures, start, end)
      val id = DatasetID.encode(DatasetID.LiikennevirastoUUID, start, end)
      println(result)
      RemoteDatasets.put(s"${URLEncoder.encode(id, "UTF-8")}.xml", result.toString)
    } finally {
      Http.shutdown()
    }
  }

  def readSpeedLimitChanges(since: Instant): Seq[Feature] = {
    val req = (changesApiUrl / "speed_limits").addQueryParameter("since", since.toString)
    val contents = Await.result(Http(req OK as.String), 30.seconds)
    (parse(contents) \ "features").extract[Seq[Feature]]
  }

  def convertToChangeDataSet(speedLimitFeatures: Seq[Feature], startTime: Instant, endTime: Instant): Any = {
    // todo: replace current time with the latest modification time from changes
    //       or specify explicitly in the change api
    <rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2" xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd" gml:id="i0fbf03ad-5c7a-4490-bb7c-64f95a91cb3c">
      { speedLimitFeatures.map(featureMember) }
      <rst:metadata>
        <rst:datasetId>{ DatasetID.encode(DatasetID.LiikennevirastoUUID, startTime, endTime)}</rst:datasetId>
        <rst:datasetCreationTime>{ endTime }</rst:datasetCreationTime>
      </rst:metadata>
      <rst:type>Update</rst:type>
    </rst:ROSATTESafetyFeatureDataset>
  }

  def splitFeaturesApplicableToBothDirections(features: Seq[Feature]): Seq[Feature] = {
    features.flatMap { feature =>
      feature.properties.sideCode match {
        case 1 =>
          Seq(feature.copy(properties = feature.properties.copy(sideCode = 2)),
            feature.copy(properties = feature.properties.copy(sideCode = 3)))
        case _ =>
          Seq(feature)
      }
    }
  }

  def featureMember(feature: Feature) = {
    val geometry = feature.geometry.coordinates.flatMap(_.take(2)).mkString(" ")
    val startMeasure = feature.properties.startMeasure
    val endMeasure = feature.properties.endMeasure
    val link = feature.properties.link
    val linkReference = "FI.1000018." + link("id").asInstanceOf[BigInt].intValue.toString
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
            <rst:id>{feature.properties.id}</rst:id>
          </rst:SafetyFeatureId>
        </rst:id>
        <rst:locationReference>
          <rst:INSPIRELinearLocation gml:id={UUID.randomUUID().toString}>
            <net:SimpleLinearReference>
              <net:element xlink:href={ linkReference }/>
              <net:applicableDirection>{ applicableDirection }</net:applicableDirection>
              <net:fromPosition uom="meter">{ startMeasure }</net:fromPosition>
              <net:toPosition uom="meter">{ endMeasure }</net:toPosition>
            </net:SimpleLinearReference>
          </rst:INSPIRELinearLocation>
        </rst:locationReference>
        <rst:locationReference>
          <rst:OpenLRLocationString gml:id={UUID.randomUUID().toString}>
            <rst:base64String>{openLR}</rst:base64String>
            <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
          </rst:OpenLRLocationString>
        </rst:locationReference>
        <rst:updateInfo>
          <rst:UpdateInfo>
            <rst:type>
              {feature.properties.changeType}</rst:type>
          </rst:UpdateInfo>
        </rst:updateInfo>
        <rst:source>Regulation</rst:source>
        <rst:encodedGeometry>
          <gml:LineString gml:id={UUID.randomUUID().toString} srsDimension="2">
            <gml:posList>{geometry}</gml:posList>
          </gml:LineString>
        </rst:encodedGeometry>
        <rst:type>SpeedLimit</rst:type>
        <rst:properties>
          <rst:SafetyFeaturePropertyValue>
            <rst:type>MaximumSpeedLimit</rst:type>
            <rst:propertyValue>
              <gml:measure uom="kmph">
                {feature.properties.value}</gml:measure>
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
      </rst:GenericSafetyFeature>
    </gml:featureMember>
  }

  private def encodeOpenLRLocationString(startMeasure: Double, endMeasure: Double, applicableDirection: String, link: Map[String, Any]): String = {
    import collection.JavaConverters._

    val points =
      link("geometry")
        .asInstanceOf[Map[String, Any]]("coordinates")
        .asInstanceOf[Seq[Seq[Double]]]
        .map { case Seq(x, y, z) => Point(x, y, z) }

    val linkGeometry =
      if (applicableDirection == "inOppositeDirection")
        points.reverse
      else
        points

    val linkLength =
      link("properties").asInstanceOf[Map[String, Any]]("length").asInstanceOf[Double]

    val line = DigiroadLine(1, linkGeometry, linkLength.round.toInt)

    val mapDatabase = new DigiroadFixtureMapDatabase(Seq(line))
    val encoder = new OpenLREncoder
    val param = new OpenLREncoderParameter.Builder()
      .`with`(mapDatabase)
      .buildParameter()

    val lineLocation =
      LocationFactory.createLineLocationWithOffsets(
        s"loc-1", Seq(line).asJava, startMeasure.round.toInt, (linkLength - endMeasure).round.toInt)
    val encoded = encoder.encodeLocation(param, lineLocation)
    val reference = encoded.getLocationReference("binary")
    val data = reference.getLocationReferenceData.asInstanceOf[ByteArray]
    new String(Base64.getEncoder.encode(data.getData), "ASCII")
  }
}

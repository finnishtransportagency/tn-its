package fi.liikennevirasto.digiroad2.tnits

import java.net.URLEncoder
import java.time.Instant
import java.util.{UUID, Base64}

import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits.GeoJson.Feature

import scala.concurrent.Await
import scala.concurrent.duration._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import sun.misc.BASE64Encoder

object GeoJson {
  case class Feature(geometry: LineString, properties: Map[String, Any])
  case class LineString(coordinates: Seq[Seq[Double]])
}

object RosatteConverter {
  protected implicit val jsonFormats: Formats = DefaultFormats

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
      val result = convertToChangeDataSet(speedLimitFeatures, start, end)
      val id = Rosatte.encodeDataSetId(Rosatte.LiikennevirastoUUID, start, end)
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
        <rst:datasetId>{ Rosatte.encodeDataSetId(Rosatte.LiikennevirastoUUID, startTime, endTime)}</rst:datasetId>
        <rst:datasetCreationTime>{ endTime }</rst:datasetCreationTime>
      </rst:metadata>
      <rst:type>Update</rst:type>
    </rst:ROSATTESafetyFeatureDataset>
  }

  def featureMember(feature: Feature) = {
    val changeType = feature.properties("changeType").asInstanceOf[String]
    val speedLimitValue = feature.properties("value").asInstanceOf[BigInt]
    val geometry = feature.geometry.coordinates.flatten.mkString(" ")
    val startMeasure = feature.properties("startMeasure").asInstanceOf[Double]
    val endMeasure = feature.properties("endMeasure").asInstanceOf[Double]
    // todo: replace hardcoded values
    <gml:featureMember>
      <rst:GenericSafetyFeature gml:id={UUID.randomUUID().toString}>
        <rst:id>
          <rst:SafetyFeatureId>
            <rst:providerId>FI.LiVi.OTH</rst:providerId>
            <rst:id>{"003c4744-28c0-42ef-be3d-107bea6bf006"}</rst:id>
          </rst:SafetyFeatureId>
        </rst:id>
        <rst:locationReference>
          <rst:INSPIRELinearLocation gml:id={UUID.randomUUID().toString}>
            <net:SimpleLinearReference>
              <net:element xlink:href="SE.TrV.NVDB:LinkSequence:1000:9729"/>
              <net:applicableDirection>inDirection</net:applicableDirection>
              <net:fromPosition uom="meter">{ startMeasure }</net:fromPosition>
              <net:toPosition uom="meter">{ endMeasure }</net:toPosition>
            </net:SimpleLinearReference>
          </rst:INSPIRELinearLocation>
        </rst:locationReference>
        <rst:locationReference>
          <rst:OpenLRLocationString gml:id={UUID.randomUUID().toString}>
            <rst:base64String>CwjjfCgGCBt5Av9MADYbCQ==</rst:base64String>
            <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
          </rst:OpenLRLocationString>
        </rst:locationReference>
        <rst:validFrom>2013-12-20</rst:validFrom>
        <rst:updateInfo>
          <rst:UpdateInfo>
            <rst:type>{changeType}</rst:type>
          </rst:UpdateInfo>
        </rst:updateInfo>
        <rst:source>Regulation</rst:source>
        <rst:encodedGeometry>
          <gml:LineString gml:id={UUID.randomUUID().toString} srsDimension="3">
            <gml:posList>{geometry}</gml:posList>
          </gml:LineString>
        </rst:encodedGeometry>
        <rst:type>SpeedLimit</rst:type>
        <rst:properties>
          <rst:SafetyFeaturePropertyValue>
            <rst:type>MaximumSpeedLimit</rst:type>
            <rst:propertyValue>
              <gml:measure uom="kmph">{speedLimitValue}</gml:measure>
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
      </rst:GenericSafetyFeature>
    </gml:featureMember>
  }
}

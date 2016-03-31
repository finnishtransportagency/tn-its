package fi.liikennevirasto.digiroad2.tnits

import java.time.Instant

import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits.GeoJson.Feature

import scala.concurrent.Await
import scala.concurrent.duration._
import org.json4s._
import org.json4s.jackson.JsonMethods._

object GeoJson {
  case class Feature(geometry: LineString, properties: Map[String, Any])
  case class LineString(coordinates: Seq[Seq[Double]])
}

object RosatteConverter {
  protected implicit val jsonFormats: Formats = DefaultFormats

  private val changesApiUrl: Req =
    (host("localhost:6666") / "digiroad" / "api" / "changes")
      .setFollowRedirects(true)
      .as(
        user = sys.env.getOrElse("CHANGE_API_USERNAME", ""),
        password = sys.env.getOrElse("CHANGE_API_PASSWORD", ""))

  def main(args: Array[String]) {
    val start = Instant.parse("2016-03-01T22:00:00Z")
    val speedLimitFeatures = readSpeedLimitChanges(start)
    println(convertToChangeDataSet(speedLimitFeatures, start))
  }

  def readSpeedLimitChanges(since: Instant): Seq[Feature] = {
    val req = (changesApiUrl / "speed_limits").addQueryParameter("since", since.toString)
    val contents = Await.result(Http(req OK as.String), 30.seconds)
    (parse(contents) \ "features").extract[Seq[Feature]]
  }

  def convertToChangeDataSet(speedLimitFeatures: Seq[Feature], startTime: Instant) = {
    // todo: replace current time with the latest modification time from changes
    //       or specify explicitly in the change api
    val endTime = Instant.now()

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
    // todo: replace hardcoded values
    <gml:featureMember>
      <rst:GenericSafetyFeature gml:id="i65736963-342f-4e0d-8c8e-76cb4d39387f">
        <rst:id>
          <rst:SafetyFeatureId>
            <rst:providerId>SE.TrV.NVDB</rst:providerId>
            <rst:id>{"003c4744-28c0-42ef-be3d-107bea6bf006"}</rst:id>
          </rst:SafetyFeatureId>
        </rst:id>
        <rst:locationReference>
          <rst:INSPIRELinearLocation gml:id="i9c28bb18-72b9-460f-b243-75d4bd2afc88">
            <net:SimpleLinearReference>
              <net:element xlink:href="SE.TrV.NVDB:LinkSequence:1000:9729"/>
              <net:applicableDirection>inDirection</net:applicableDirection>
              <net:fromPosition uom="meter">0</net:fromPosition>
              <net:toPosition uom="meter">127.108815059772</net:toPosition>
            </net:SimpleLinearReference>
          </rst:INSPIRELinearLocation>
        </rst:locationReference>
        <rst:locationReference>
          <rst:OpenLRLocationString gml:id="i2e8364eb-eb2e-4fee-a8ec-4e425ffc0866">
            <rst:base64String>CwjjfCgGCBt5Av9MADYbCQ==</rst:base64String>
            <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
          </rst:OpenLRLocationString>
        </rst:locationReference>
        <rst:validFrom>2013-12-20</rst:validFrom>
        <rst:updateInfo>
          <rst:UpdateInfo>
            <rst:type>Remove</rst:type>
          </rst:UpdateInfo>
        </rst:updateInfo>
        <rst:source>Regulation</rst:source>
        <rst:encodedGeometry>
          <gml:LineString gml:id="ibce9895a-b1b8-47be-81a4-99ea59f4cfe6" srsDimension="2">
            <gml:posList>
              12.4996131654393 56.2831283339562 12.4983811088529 56.2835169017528 12.4979903270137 56.2836199295346 12.4978083213852 56.2836708417839
            </gml:posList>
          </gml:LineString>
        </rst:encodedGeometry>
        <rst:type>RoadNumber</rst:type>
        <rst:properties>
          <rst:SafetyFeaturePropertyValue>
            <rst:type>RoadNumber</rst:type>
            <rst:propertyValue>
              <gml:measure uom="">111</gml:measure>
            </rst:propertyValue>
          </rst:SafetyFeaturePropertyValue>
        </rst:properties>
      </rst:GenericSafetyFeature>
    </gml:featureMember>
  }
}

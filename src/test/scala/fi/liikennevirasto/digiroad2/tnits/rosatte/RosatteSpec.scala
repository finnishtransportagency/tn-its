package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID.DataSetId
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.FunSuite

class RosatteSpec extends FunSuite {
  protected implicit val jsonFormats: Formats = DefaultFormats

  test("Dataset IDs should rountrip") {
    val uuid = UUID.randomUUID()
    val start = Instant.now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)
    val end = Instant.now.truncatedTo(ChronoUnit.SECONDS)

    val encodedDataSetId = DatasetID.encode(uuid, start, end)
    val DataSetId(decodedUuid, decodedStart, decodedEnd) = DatasetID.decode(encodedDataSetId)

    assert(decodedUuid == uuid)
    assert(decodedStart == start)
    assert(decodedEnd == end)
  }

  test("Empty conversion") {
    val input = """{"type":"FeatureCollection","features":[]}"""

    val parsed = (parse(input) \ "features").extract[Seq[Asset]]
    val output = new ByteArrayOutputStream()

    RosatteConverter.convertDataSet(
      featureMembers = Seq((AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"), parsed)),
      start = Instant.parse("2014-04-22T13:00:00Z"),
      end = Instant.parse("2014-04-22T15:00:00Z"),
      dataSetId = "id",
      output = output
    )

    assertConversion(output,
      """<rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink"
            xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd"
            xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2"
            xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst"
            xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd"
            gml:id="ID-FOO"><rst:metadata>
            <rst:datasetId>id</rst:datasetId>
            <rst:datasetCreationTime>2014-04-22T15:00:00Z</rst:datasetCreationTime>
          </rst:metadata>
          <rst:type>Update</rst:type>
        </rst:ROSATTESafetyFeatureDataset>""")
  }

  test("Simple conversion") {
    val input =
      """{"type":"FeatureCollection","features":[{
        "geometry": {
            "coordinates": [
                [
                    373809.235,
                    6677797.194,
                    0.0
                ],
                [
                    373857.037,
                    6677822.138,
                    0.0
                ],
                [
                    373861.411,
                    6677824.325,
                    0.0
                ],
                [
                    373881.506,
                    6677834.77,
                    0.0
                ],
                [
                    373903.676,
                    6677845.834,
                    0.0
                ],
                [
                    373913.223,
                    6677851.591,
                    0.0
                ],
                [
                    373917.927,
                    6677859.733,
                    0.0
                ],
                [
                    373918.6579676908,
                    6677868.98559103,
                    0.0
                ]
            ],
            "type": "LineString"
        },
        "id": 200277,
        "properties": {
            "changeType": "Modify",
            "createdAt": "11.04.2016 15:44:09",
            "endMeasure": 136.067,
            "link": {
                "geometry": {
                    "coordinates": [
                        [
                            373809.235,
                            6677797.194,
                            0.0
                        ],
                        [
                            373857.037,
                            6677822.138,
                            0.0
                        ],
                        [
                            373861.411,
                            6677824.325,
                            0.0
                        ],
                        [
                            373881.506,
                            6677834.77,
                            0.0
                        ],
                        [
                            373903.676,
                            6677845.834,
                            0.0
                        ],
                        [
                            373913.223,
                            6677851.591,
                            0.0
                        ],
                        [
                            373917.927,
                            6677859.733,
                            0.0
                        ],
                        [
                            373918.658,
                            6677868.986,
                            0.0
                        ]
                    ],
                    "type": "LineString"
                },
                "id": 1611289,
                "properties": {
                    "functionalClass": 4,
                    "length": 136.06741024424275,
                    "type": 3
                },
                "type": "Feature"
            },
            "modifiedAt": "11.05.2016 14:47:56",
            "modifiedBy": "test2",
            "sideCode": 1,
            "startMeasure": 0.0,
            "value": 120
        },
        "type": "Feature"}]}"""

    val parsed = (parse(input) \ "features").extract[Seq[Asset]]
    val output = new ByteArrayOutputStream()

    RosatteConverter.convertDataSet(
      featureMembers = Seq((AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"), parsed)),
      start = Instant.parse("2014-04-22T13:00:00Z"),
      end = Instant.parse("2014-04-22T15:00:00Z"),
      dataSetId = "id",
      output = output
    )

    assertConversion(output,
      """<rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink"
            xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd"
            xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2"
            xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst"
            xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd"
            gml:id="ID-FOO"><gml:featureMember>
          <rst:GenericSafetyFeature gml:id="ID-FOO">
            <rst:id>
              <rst:SafetyFeatureId>
                <rst:providerId>FI.LiVi.OTH</rst:providerId>
                <rst:id>200277</rst:id>
              </rst:SafetyFeatureId>
            </rst:id>
            <rst:locationReference>
              <rst:INSPIRELinearLocation gml:id="ID-FOO">
                <net:SimpleLinearReference>
                  <net:element xlink:href="FI.1000018.1611289"/>
                  <net:applicableDirection>inDirection</net:applicableDirection>
                  <net:fromPosition uom="meter">0.0</net:fromPosition>
                  <net:toPosition uom="meter">136.067</net:toPosition>
                </net:SimpleLinearReference>
              </rst:INSPIRELinearLocation>
            </rst:locationReference>
            <rst:locationReference>
              <rst:OpenLRLocationString gml:id="ID-FOO">
                <rst:base64String>CxGUkirSPhtlAgDBAEQbEQ==</rst:base64String>
                <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
              </rst:OpenLRLocationString>
            </rst:locationReference>
            <rst:updateInfo>
              <rst:UpdateInfo>
                <rst:type>Modify</rst:type>
              </rst:UpdateInfo>
            </rst:updateInfo>
            <rst:source>Regulation</rst:source>
            <rst:encodedGeometry>
              <gml:LineString gml:id="ID-FOO" srsDimension="2">
                <gml:posList>
                  24.722351911463946 60.21737240496545 24.723198383556337 60.217611001297584 24.723275897827786 60.21763197693 24.723631767327714 60.21773190877784 24.72402467212656 60.217838035456914 24.724193250962678 60.217892641090586 24.724273013546654 60.21796714581161 24.72428044017068 60.21805038514444
                </gml:posList>
              </gml:LineString>
            </rst:encodedGeometry>
            <rst:type>SpeedLimit</rst:type>
            <rst:properties>
              <rst:SafetyFeaturePropertyValue>
                <rst:type>MaximumSpeedLimit</rst:type>
                <rst:propertyValue>
                  <gml:measure uom="kmph">120</gml:measure>
                </rst:propertyValue>
              </rst:SafetyFeaturePropertyValue>
            </rst:properties>
          </rst:GenericSafetyFeature>
        </gml:featureMember><gml:featureMember>
          <rst:GenericSafetyFeature gml:id="ID-FOO">
            <rst:id>
              <rst:SafetyFeatureId>
                <rst:providerId>FI.LiVi.OTH</rst:providerId>
                <rst:id>200277</rst:id>
              </rst:SafetyFeatureId>
            </rst:id>
            <rst:locationReference>
              <rst:INSPIRELinearLocation gml:id="ID-FOO">
                <net:SimpleLinearReference>
                  <net:element xlink:href="FI.1000018.1611289"/>
                  <net:applicableDirection>inOppositeDirection</net:applicableDirection>
                  <net:fromPosition uom="meter">0.0</net:fromPosition>
                  <net:toPosition uom="meter">136.067</net:toPosition>
                </net:SimpleLinearReference>
              </rst:INSPIRELinearLocation>
            </rst:locationReference>
            <rst:locationReference>
              <rst:OpenLRLocationString gml:id="ID-FOO">
                <rst:base64String>CxGU7CrSXRtxAv8//7wbBQ==</rst:base64String>
                <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
              </rst:OpenLRLocationString>
            </rst:locationReference>
            <rst:updateInfo>
              <rst:UpdateInfo>
                <rst:type>Modify</rst:type>
              </rst:UpdateInfo>
            </rst:updateInfo>
            <rst:source>Regulation</rst:source>
            <rst:encodedGeometry>
              <gml:LineString gml:id="ID-FOO" srsDimension="2">
                <gml:posList>
                  24.722351911463946 60.21737240496545 24.723198383556337 60.217611001297584 24.723275897827786 60.21763197693 24.723631767327714 60.21773190877784 24.72402467212656 60.217838035456914 24.724193250962678 60.217892641090586 24.724273013546654 60.21796714581161 24.72428044017068 60.21805038514444
                </gml:posList>
              </gml:LineString>
            </rst:encodedGeometry>
            <rst:type>SpeedLimit</rst:type>
            <rst:properties>
              <rst:SafetyFeaturePropertyValue>
                <rst:type>MaximumSpeedLimit</rst:type>
                <rst:propertyValue>
                  <gml:measure uom="kmph">120</gml:measure>
                </rst:propertyValue>
              </rst:SafetyFeaturePropertyValue>
            </rst:properties>
          </rst:GenericSafetyFeature>
        </gml:featureMember><rst:metadata>
            <rst:datasetId>id</rst:datasetId>
            <rst:datasetCreationTime>2014-04-22T15:00:00Z</rst:datasetCreationTime>
          </rst:metadata>
          <rst:type>Update</rst:type>
        </rst:ROSATTESafetyFeatureDataset>""")
  }

  def assertConversion(result: ByteArrayOutputStream, expected: String): Unit = {
    assertResult(expected.filterNot(_.isWhitespace))(normalize(result))
  }

  def normalize(output: ByteArrayOutputStream): String = {
    output.toString("UTF-8").replaceAll("""gml:id="[^"]+?"""", """gml:id="ID-FOO"""").filterNot(_.isWhitespace)
  }
}
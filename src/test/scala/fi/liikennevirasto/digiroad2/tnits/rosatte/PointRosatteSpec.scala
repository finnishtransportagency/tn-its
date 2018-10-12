package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.ByteArrayOutputStream
import java.time.Instant

import fi.liikennevirasto.digiroad2.tnits.geojson.{Feature, FeaturePoint}
import fi.liikennevirasto.digiroad2.tnits.oth.PedestrianCrossingOTHClient
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.PedestrianCrossingAssetProperties
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}
import org.scalatest.FunSuite

class PointRosatteSpec extends FunSuite {
  protected implicit val jsonFormats: Formats = DefaultFormats


  test("Simple conversion") {
    val input =
      """{
        "type": "FeatureCollection",
        "features": [
          {
            "type": "Feature",
            "id": 615883,
            "geometry": {
            "type": "Point",
            "coordinates": [
                  532221.827,
                  6993990.817,
                  128.55299999999988
              ]
            },
            "properties": {
            "changeType": "Add",
            "sideCode": 1,
            "link": {
            "type": "Feature",
            "id": 5170291,
            "geometry": {
                "type": "LineString",
                "coordinates": [
                    [
                        532221.827,
                        6993990.817,
                        128.55299999999988
                    ],
                    [
                        532236.331,
                        6993995.433,
                        128.5749999999971
                    ],
                    [
                        532253.252,
                        6994000.208,
                        128.7960000000021
                    ],
                    [
                        532272.138,
                        6994005.12,
                        129.51499999999942
                    ]
                ]
            },
            "properties": {
                "functionalClass": 6,
                "type": 3,
                "length": 20.41485588228664
            }
          },
          "createdAt": "11.05.2016 14:47:56",
          "createdBy": "silari",
          "mValue": 5.005
        }
      }
    ]}"""

    val parsed = (parse(input) \ "features").extract[Seq[FeaturePoint[PedestrianCrossingAssetProperties]]].asInstanceOf[Seq[Feature[AssetProperties]]]
    val output = new ByteArrayOutputStream()

    RosatteConverter.convertDataSet(
      featureMembers = Seq((AssetType("pedestrian_crossing", "PedestrianCrossing", "PedestrianCrossing", "", PedestrianCrossingOTHClient, PointRosatteConverter), parsed)),
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
                <rst:id>615883</rst:id>
              </rst:SafetyFeatureId>
            </rst:id>
            <rst:locationReference>
              <rst:INSPIRELinearLocation gml:id="ID-FOO">
                <net:SimpleLinearReference>
                  <net:element xlink:href="FI.1000018.5170291"/>
                  <net:applicableDirection>bothDirection</net:applicableDirection>
                  <net:atPosition uom="meter">5.005</net:atPosition>
                </net:SimpleLinearReference>
              </rst:INSPIRELinearLocation>
            </rst:locationReference>
            <rst:locationReference>
              <rst:OpenLRLocationString gml:id="ID-FOO">
                <rst:base64String>CxOnTSzaRSumAABkAAwrdkDA</rst:base64String>
                <rst:OpenLRBinaryVersion>1.4</rst:OpenLRBinaryVersion>
              </rst:OpenLRLocationString>
            </rst:locationReference>
            <rst:updateInfo>
              <rst:UpdateInfo>
                <rst:type>Add</rst:type>
              </rst:UpdateInfo>
            </rst:updateInfo>
            <rst:source>Regulation</rst:source>
            <rst:encodedGeometry>
              <gml:LineString gml:id="ID-FOO" srsDimension="2">
                <gml:posList>
                  27.637756287723434 63.073971313222586
                  </gml:posList>
              </gml:LineString>
            </rst:encodedGeometry>
            <rst:type>PedestrianCrossing</rst:type>
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
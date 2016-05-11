package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.geojson
import com.sun.org.apache.xalan.internal.xsltc.runtime.output.StringOutputBuffer
import fi.liikennevirasto.digiroad2.tnits.geojson.{Feature, LineString}
import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID.DataSetId
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.{AssetProperties, RoadLinkProperties}
import fi.liikennevirasto.digiroad2.tnits.runners.AssetType
import openlr.map.FunctionalRoadClass
import org.scalatest.FunSuite

import scala.xml.XML

class RosatteSpec extends FunSuite {
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
    val output = new ByteArrayOutputStream()

    RosatteConverter.convertDataSet(
      featureMembers = Seq(),
      start = Instant.parse("2014-04-22T13:00:00Z"),
      end = Instant.parse("2014-04-22T15:00:00Z"),
      dataSetId = "id",
      output = output
    )

    val root = XML.loadString(output.toString("UTF-8"))

    assert(root.label == "ROSATTESafetyFeatureDataset")

    assert((root \ "type").text == "Update")

    val metadata = root \ "metadata"

    assert((metadata \ "datasetId").text == "id")
    assert((metadata \ "datasetCreationTime").text == "2014-04-22T15:00:00Z")
  }

  test("Simple conversion") {
    val output = new ByteArrayOutputStream()

    RosatteConverter.convertDataSet(
      featureMembers = Seq((AssetType("speed_limits", "SpeedLimit", "MaximumSpeedLimit", "kmph"), Seq(
        geojson.Feature[AssetProperties](
          id = 1,
          geometry = LineString(Seq(Seq(0, 0), Seq(20, 0))),
          properties = AssetProperties(
            sideCode = 1,
            changeType = "Update",
            value = 80,
            startMeasure = 0,
            endMeasure = 20,
            link = Feature[RoadLinkProperties](
              id = 1,
              geometry = LineString(Seq(Seq(0, 0), Seq(100, 0))),
              properties = RoadLinkProperties(
                functionalClass = 1,
                `type` = 1,
                length = 100
              )
            )))
      ))),
      start = Instant.parse("2014-04-22T13:00:00Z"),
      end = Instant.parse("2014-04-22T15:00:00Z"),
      dataSetId = "id",
      output = output
    )

    val root = XML.loadString(output.toString("UTF-8"))


  }

//"""<rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink"
//            xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd"
//            xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2"
//            xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//            xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst"
//            xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd"
//            gml:id="47a52036-c32f-4898-af1d-f8a6319655a4"><rst:metadata>
//            <rst:datasetId>id</rst:datasetId>
//            <rst:datasetCreationTime>2014-04-22T15:00:00Z</rst:datasetCreationTime>
//          </rst:metadata>
//          <rst:type>Update</rst:type>
//        </rst:ROSATTESafetyFeatureDataset>"""
}
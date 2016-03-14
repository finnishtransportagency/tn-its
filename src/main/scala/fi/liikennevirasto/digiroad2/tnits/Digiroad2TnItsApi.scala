package fi.liikennevirasto.digiroad2.tnits

import org.scalatra._

class Digiroad2TnItsApi extends ScalatraServlet {
  before() {
    contentType = "application/xml"
  }

  get("/rosattedownload/download/queryDataSets") {
    params.get("lastValidDatasetId").map { lastValidDatasetId =>
      <rst:ROSATTERestDatasetRefList xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <rst:ROSATTERestDatasetRef xlink:href="/rosattedownload/download/readDataSet?dataSetID=1uShiYqi%2fEe120s9P1ga7AAAAN2DTdVBAAAAcIdN1UE%3d"/>
      </rst:ROSATTERestDatasetRefList>
    }.getOrElse {
      <rst:ROSATTERestDatasetRefList xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <rst:ROSATTERestDatasetRef xlink:href="/rosattedownload/download/readDataSet?dataSetID=1uShiYqi%2fEe120s9P1ga7AAAAPSdSdVBAAAAHEdL1UE%3d"/>
        <rst:ROSATTERestDatasetRef xlink:href="/rosattedownload/download/readDataSet?dataSetID=1uShiYqi%2fEe120s9P1ga7AAAAGjrS9VBAAAA%2f39N1UE%3d"/>
        <rst:ROSATTERestDatasetRef xlink:href="/rosattedownload/download/readDataSet?dataSetID=1uShiYqi%2fEe120s9P1ga7AAAADiDTdVBAAAA3YNN1UE%3d"/>
        <rst:ROSATTERestDatasetRef xlink:href="/rosattedownload/download/readDataSet?dataSetID=1uShiYqi%2fEe120s9P1ga7AAAAN2DTdVBAAAAcIdN1UE%3d"/>
      </rst:ROSATTERestDatasetRefList>
    }
  }

  get("/rosattedownload/download/readDataSet") {
    println("/rosattedownload/download/readDataSet", params)
    val id = params.getOrElse("dataSetID", halt(400))

    <rst:ROSATTESafetyFeatureDataset xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:net="urn:x-inspire:specification:gmlas:Network:3.2" xmlns:openlr="http://www.openlr.org/openlr" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:TPEG="TPEG" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xsi:schemaLocation="http://www.ertico.com/en/subprojects/rosatte/rst http://rosatte-no.triona.se/schemas/Rosatte.xsd" gml:id="i0fbf03ad-5c7a-4490-bb7c-64f95a91cb3c">
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
      <gml:featureMember>
        <rst:GenericSafetyFeature gml:id="if6825080-cb2f-442a-853e-9acce64500df">
          <rst:id>
            <rst:SafetyFeatureId>
              <rst:providerId>SE.TrV.NVDB</rst:providerId>
              <rst:id>{"014237b3-315d-4689-b97f-9315402f1744"}</rst:id>
            </rst:SafetyFeatureId>
          </rst:id>
          <rst:locationReference>
            <rst:INSPIRELinearLocation gml:id="i1ea428eb-4733-4cec-adaf-5ba757f15532">
              <net:SimpleLinearReference>
                <net:element xlink:href="SE.TrV.NVDB:LinkSequence:1000:10154"/>
                <net:applicableDirection>inDirection</net:applicableDirection>
                <net:fromPosition uom="meter">0</net:fromPosition>
                <net:toPosition uom="meter">13.484851537724</net:toPosition>
              </net:SimpleLinearReference>
            </rst:INSPIRELinearLocation>
          </rst:locationReference>
          <rst:locationReference>
            <rst:OpenLRLocationString gml:id="ia5c44167-541a-4120-a9f7-6d0cc8877d0e">
              <rst:base64String>CwkTIyfWBB5mAAATAAYeFg==</rst:base64String>
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
            <gml:LineString gml:id="i393368a5-75cc-421f-b6ee-63423a284a76" srsDimension="2">
              <gml:posList>
                12.7613765208624 56.0193787385888 12.7615675754065 56.0194355029514
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
      <rst:metadata>
        <rst:datasetId>1uShiYqi/Ee120s9P1ga7AAAADiDTdVBAAAA3YNN1UE=</rst:datasetId>
        <rst:datasetCreationTime>2015-04-23T12:44:15</rst:datasetCreationTime>
      </rst:metadata>
      <rst:type>Update</rst:type>
    </rst:ROSATTESafetyFeatureDataset>
  }
}

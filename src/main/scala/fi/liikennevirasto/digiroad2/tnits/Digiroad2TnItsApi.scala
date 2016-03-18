package fi.liikennevirasto.digiroad2.tnits

import org.scalatra._

class Digiroad2TnItsApi extends ScalatraServlet {

  get("/rosattedownload/download/queryDataSets") {
    contentType = "application/xml"
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
    val id = params.getOrElse("dataSetID", halt(BadRequest("Missing mandatory parameter 'dataSetID'")))
    Option(getServletContext.getResourceAsStream("/RosatteTestData/" + id + ".xml")).map { dataStream =>
      contentType = "application/xml"
      dataStream
    }.getOrElse(NotFound("No dataset found for id " + id))
  }
}

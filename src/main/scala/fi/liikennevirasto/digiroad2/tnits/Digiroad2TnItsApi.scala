package fi.liikennevirasto.digiroad2.tnits

import java.net.URL
import java.nio.file.FileSystems

import org.scalatra._
import collection.JavaConverters._

class Digiroad2TnItsApi extends ScalatraServlet {

  get("/rosattedownload/download/queryDataSets") {
    contentType = "application/xml"

    val dataSetPaths = getServletContext.getResourcePaths("/RosatteTestData/").asScala
    val dataSetIds = dataSetPaths.map { _.drop("/RosatteTestData/".length).dropRight(".xml".length) }

    params.get("lastValidDatasetId").map { lastValidDatasetId =>
      <rst:ROSATTERestDatasetRefList xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <rst:ROSATTERestDatasetRef xlink:href="/rosattedownload/download/readDataSet?dataSetID=1uShiYqi%2fEe120s9P1ga7AAAAN2DTdVBAAAAcIdN1UE%3d"/>
      </rst:ROSATTERestDatasetRefList>
    }.getOrElse {
      val dataSetUrls = dataSetIds.map { dataSetId =>
        "http://localhost:8080/rosattedownload/download/readDataSet?dataSetID=" + java.net.URLEncoder.encode(dataSetId, "UTF-8")
      }

      <rst:ROSATTERestDatasetRefList xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        {
        dataSetUrls.map { url => <rst:ROSATTERestDatasetRef xlink:href={ url }/> }
        }
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

package fi.liikennevirasto.digiroad2.tnits

import java.net.{URLDecoder, URLEncoder}

import org.scalatra._

import scala.collection.JavaConverters._

class Digiroad2TnItsApi extends ScalatraServlet {

  get("/rosattedownload/download/queryDataSets") {
    contentType = "application/xml"

    val dataSetPaths =
      getServletContext.getResourcePaths("/RosatteTestData/").asScala

    val dataSetIds =
      dataSetPaths
        .toSeq
        .map { _.drop("/RosatteTestData/".length).dropRight(".xml".length) }
        .map { id => (id, Rosatte.decodeDataSetId(URLDecoder.decode(id, "UTF-8"))) }
        .sortBy { case (_, id) => id.startDate }

    val wantedDataSetIds =
      if (params.contains("lastValidDatasetId")) {
        val lastValidDataSetId =
          params("lastValidDatasetId")
        val wantedId =
          Rosatte.decodeDataSetId(lastValidDataSetId)
        dataSetIds
          .filter { case (_, id) => id.startDate.isAfter(wantedId.startDate) }
          .map(_._1)
      } else {
        dataSetIds
          .map(_._1)
      }

    <rst:ROSATTERestDatasetRefList xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      {wantedDataSetIds.map(dataSetElement)}
    </rst:ROSATTERestDatasetRefList>
  }

  get("/rosattedownload/download/readDataSet") {
    val id = params.getOrElse("dataSetID", halt(BadRequest("Missing mandatory parameter 'dataSetID'")))
    Option(getServletContext.getResourceAsStream("/RosatteTestData/" + id + ".xml")).map { dataStream =>
      contentType = "application/xml"
      dataStream
    }.getOrElse(NotFound("No dataset found for id " + id))
  }

  def dataSetElement(id: String) = {
    val scheme = request.urlScheme.toString.toLowerCase
    val port = serverPort
    val url =
      (scheme, port) match {
        case ("http", 80) | ("https", 443) =>
          s"$scheme://$serverHost/rosattedownload/download/readDataSet?dataSetID=" + URLEncoder.encode(id, "UTF-8")
        case _ =>
          s"$scheme://$serverHost:$port/rosattedownload/download/readDataSet?dataSetID=" + URLEncoder.encode(id, "UTF-8")
      }
    <rst:ROSATTERestDatasetRef xlink:href={url}/>
  }
}

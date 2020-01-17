package fi.liikennevirasto.digiroad2.tnits.api

import java.net.URLDecoder
import javax.servlet.http.HttpServletRequest
import fi.liikennevirasto.digiroad2.tnits.aineistot.{RemoteDataset, RemoteNonStdDataset}
import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID
import org.scalatra._

import scala.concurrent.ExecutionContext

/** The public TN-ITS API providing endpoints to query and download change datasets.
  */
class Digiroad2TnItsApi extends ScalatraServlet with FutureSupport {

  override protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.global

  get("/download/queryDataSets/nonstd") {
    contentType = "application/xml"

    val dataSetIds = RemoteNonStdDataset.index
    queryDataSets(dataSetIds)
  }

  get("/download/queryDataSets") {
    contentType = "application/xml"

    val dataSetIds = RemoteDataset.index
    queryDataSets(dataSetIds)
  }

  get("/download/readDataSet/nonstd") {
    contentType = "application/xml"
    val id = params.getOrElse("dataSetID", halt(BadRequest("Missing mandatory parameter 'dataSetID'")))
    RemoteNonStdDataset.get(id)
  }

  get("/download/readDataSet") {
    contentType = "application/xml"
    val id = params.getOrElse("dataSetID", halt(BadRequest("Missing mandatory parameter 'dataSetID'")))
    RemoteDataset.get(id)
  }

  def queryDataSets(dataSetIds: Seq[String]): Unit = {
    val decodedDataSetIds =
      dataSetIds
        .map { id => (id, DatasetID.decode(URLDecoder.decode(id, "UTF-8"))) }
        .sortBy { case (_, id) => id.startDate }

    val wantedDataSetIds =
      if (params.contains("lastValidDatasetId")) {
        val lastValidDataSetId = params("lastValidDatasetId")
        val wantedId = DatasetID.decode(lastValidDataSetId)
        decodedDataSetIds
          .filter { case (_, id) => id.startDate.isAfter(wantedId.startDate) }
          .map(_._1)
      } else {
        decodedDataSetIds
          .map(_._1)
      }

    <rst:ROSATTERestDatasetRefList xmlns:xlink="http://www.w3.org/1999/xlink"
                                   xmlns:rst="http://www.ertico.com/en/subprojects/rosatte/rst"
                                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      {wantedDataSetIds.map(dataSetElement)}
    </rst:ROSATTERestDatasetRefList>
  }

  def dataSetElement(id: String)(implicit request: HttpServletRequest) = {
    val scheme = request.urlScheme.toString.toLowerCase
    val port = serverPort
    val url =
      (scheme, port) match {
        case ("http", 80) | ("https", 443) =>
          s"$scheme://$serverHost/rosattedownload/download/readDataSet?dataSetID=" + id
        case _ =>
          s"$scheme://$serverHost:$port/rosattedownload/download/readDataSet?dataSetID=" + id
      }
    <rst:ROSATTERestDatasetRef xlink:href={url}/>
  }

}

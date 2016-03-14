package fi.liikennevirasto.digiroad2.tnits

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import org.scalatra._

class Digiroad2TnItsApi extends ScalatraServlet with JacksonJsonSupport  {
  protected implicit val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/RosatteDownload/download/querydatasets") {
    params.get("lastValidDatasetId").map { lastValidDatasetId =>
      Map("TODO" -> s"implement dataset query to fetch datasets after $lastValidDatasetId")
    }.getOrElse {
      Map("TODO" -> "implement dataset query to fetch all datasets")
    }
  }
}

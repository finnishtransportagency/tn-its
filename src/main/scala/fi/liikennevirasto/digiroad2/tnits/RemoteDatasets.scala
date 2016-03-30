package fi.liikennevirasto.digiroad2.tnits

import java.io.InputStream
import java.net.URLDecoder

import dispatch._
import Defaults._
import com.ning.http.client.Response
import org.jsoup.Jsoup

import scala.collection.JavaConverters._

object RemoteDatasets {
  private val baseUrl: Req =
    (host("aineistot.liikennevirasto.fi") / "digiroad" / "tnits-testdata")
      .setFollowRedirects(true)
      .as(
        user = sys.env.getOrElse("AINEISTOT_USERNAME", ""),
        password = sys.env.getOrElse("AINEISTOT_PASSWORD", ""))

  private def dataSetUrl(id: String): Req =
    baseUrl / (id + ".xml")

  def index: Future[Seq[String]] =
    Http(baseUrl OK as.String).map { contents =>
      val doc = Jsoup.parse(contents, baseUrl.url)
      val links = doc.select("a")
      links.asScala
        .map(_.attr("href"))
        .filter(_.endsWith(".xml"))
        .map(_.dropRight(".xml".length))
        .map(URLDecoder.decode(_, "UTF-8"))
    }

  def get(id: String): Future[InputStream] =
    Http(dataSetUrl(id) OK as.Response(_.getResponseBodyAsStream))
}

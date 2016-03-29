package fi.liikennevirasto.digiroad2.tnits

import dispatch._
import Defaults._
import org.jsoup.Jsoup
import scala.collection.JavaConverters._

object RemoteDatasets {
  private val baseUrl = "http://aineistot.liikennevirasto.fi/digiroad/tnits-testdata/"

  private val indexUrl =
    url(baseUrl)
      .as(
        user = sys.env.getOrElse("AINEISTOT_USERNAME", ""),
        password = sys.env.getOrElse("AINEISTOT_PASSWORD", ""))

  def index: Future[Seq[String]] =
    Http(indexUrl OK as.String).map { contents =>
      val doc = Jsoup.parse(contents, baseUrl)
      val links = doc.select("a")
      links.asScala.map(_.attr("href")).filter(_.endsWith(".xml"))
    }
}

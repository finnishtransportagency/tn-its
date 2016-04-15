package fi.liikennevirasto.digiroad2.tnits.aineistot

import java.io.{ByteArrayInputStream, InputStream}
import java.net.{URLDecoder, URLEncoder}
import java.time.Instant

import com.ning.http.client.ProxyServer
import com.ning.http.client.ProxyServer.Protocol
import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID
import org.apache.commons.net.ftp.FTPClient
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object RemoteDatasets {
  private val logins =
    config.logins.aineistot

  val baseUrl =
    url(config.urls.aineistot.dir)
      .setFollowRedirects(true)
      .as(
        user = logins.username,
        password = logins.password)

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

  def getLatestEndTime: Option[Instant] = {
    val dataSets = Await.result (RemoteDatasets.index, 30.seconds)

    if (dataSets.nonEmpty) {
      Some(dataSets
        .map { id => DatasetID.decode(URLDecoder.decode(id, "UTF-8")) }
        .map { _.endDate }
        .max)
    } else {
      None
    }
  }

  def get(id: String): Future[InputStream] =
    Http(dataSetUrl(id) OK as.Response(_.getResponseBodyAsStream))

  def put(filename: String, contents: String): Unit = {
    val client = new FTPClient
    client.connect(config.urls.aineistot.ftp)
    if (!client.login(logins.username, logins.password))
      throw new IllegalArgumentException("Login failed")
    if (!client.changeWorkingDirectory(config.dir))
      throw new IllegalStateException("No such directory: tn-its")
    val files = client.listNames()
    if (files == null)
      throw new IllegalStateException(client.getReplyString)
    if (files.contains(filename))
      throw new IllegalArgumentException(s"$filename already exists on server")
    if (!client.storeFile(filename, new ByteArrayInputStream(contents.getBytes("UTF-8"))))
      throw new IllegalStateException(s"Error saving file `$filename`: ${client.getReplyString}")
    if (!client.logout())
      throw new IllegalStateException(client.getReplyString)
    client.disconnect()
  }

  private def dataSetUrl(id: String): Req =
    baseUrl / (URLEncoder.encode(id, "UTF-8") + ".xml")
}

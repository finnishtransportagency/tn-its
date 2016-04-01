package fi.liikennevirasto.digiroad2.tnits

import java.io.{ByteArrayInputStream, InputStream}
import java.net.{URLDecoder, URLEncoder}

import dispatch.Defaults._
import dispatch._
import org.apache.commons.net.ftp.{FTPClient, FTPClientConfig}
import org.jsoup.Jsoup

import scala.collection.JavaConverters._
import scala.io.Source

object RemoteDatasets {
  private val baseUrl: Req =
    (host("aineistot.liikennevirasto.fi") / "digiroad" / "tnits-testdata")
      .setFollowRedirects(true)
      .as(
        user = sys.env.getOrElse("AINEISTOT_USERNAME", ""),
        password = sys.env.getOrElse("AINEISTOT_PASSWORD", ""))

  private def dataSetUrl(id: String): Req =
    baseUrl / (URLEncoder.encode(id, "UTF-8") + ".xml")

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

  def put(filename: String, contents: String): Unit = {
    val client = new FTPClient
    println("Connect")
    client.connect("aineistot.liikennevirasto.fi")
    val username = sys.env.getOrElse("AINEISTOT_USERNAME", "")
    val password = sys.env.getOrElse("AINEISTOT_PASSWORD", "")
    println("Login")
    if (!client.login(username, password))
      throw new IllegalArgumentException("Login failed")
    println("Change directory")
    if (!client.changeWorkingDirectory("tnits-converter"))
      throw new IllegalStateException("No such directory: tn-its")
    println("List files")
    val files = client.listNames()
    if (files == null)
      throw new IllegalStateException(client.getReplyString)
    if (files.contains(filename))
      throw new IllegalArgumentException(s"$filename already exists on server")
    println("Save file")
    if (!client.storeFile(filename, new ByteArrayInputStream(contents.getBytes("UTF-8"))))
      throw new IllegalStateException(s"Error saving file `$filename`: ${client.getReplyString}")
    println("Logout")
    if (!client.logout())
      throw new IllegalStateException(client.getReplyString)
    println("Disconnect")
    client.disconnect()
    println("Done")
  }
}

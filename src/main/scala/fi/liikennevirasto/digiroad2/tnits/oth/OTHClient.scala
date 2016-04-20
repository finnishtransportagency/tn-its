package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant
import java.util.concurrent.CancellationException

import com.ning.http.client.ProxyServer
import com.ning.http.client.ProxyServer.Protocol
import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.concurrent.FutureCallback
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.{HttpHost, HttpResponse}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats

  private val changesApiUrl: Req = {
    val req = url(config.urls.changesApi)
      .setFollowRedirects(true)
      .as(
        user = config.logins.oth.username,
        password = config.logins.oth.password)

    if (config.optionalProxy.isDefined) {
      val proxy = config.optionalProxy.get
      println(s"Using proxy: $proxy")
      req.setProxyServer(new ProxyServer(Protocol.HTTP, proxy.host, proxy.port, proxy.username, proxy.password))
    } else {
      println("Not using proxy")
      req
    }
  }

  def fetchChangesWithApacheHttpAsyncClient(httpClient: CloseableHttpAsyncClient, apiEndPoint: String, since: Instant, until: Instant): scala.concurrent.Future[HttpResponse] = {
    val url = (changesApiUrl / apiEndPoint).url

    val promise = scala.concurrent.Promise[HttpResponse]()

    val config =
      if (tnits.config.optionalProxy.isDefined) {
        val proxy = tnits.config.optionalProxy.get
        RequestConfig.custom()
          .setProxy(new HttpHost(proxy.host, proxy.port))
          .build()
      } else {
        RequestConfig.DEFAULT
      }

    val get = new HttpGet(url)
    get.setConfig(config)
    println(s"*** Request: ${get.getRequestLine}")
    httpClient
      .execute(get, new FutureCallback[HttpResponse] {
        override def cancelled(): Unit =
          promise.failure(new CancellationException)

        override def completed(t: HttpResponse): Unit =
          promise.success(t)

        override def failed(e: Exception): Unit =
          promise.failure(e)
      })

    promise.future
  }

  def fetchChanges(apiEndpoint: String, since: Instant, until: Instant): scala.concurrent.Future[Seq[Asset]] = {
    val req = (changesApiUrl / apiEndpoint)
      .addQueryParameter("since", since.toString)
      .addQueryParameter("until", until.toString)

    println(s"Request: ${req.url}")

    Http(req OK as.String)
      .map { contents =>
        (parse(contents) \ "features").extract[Seq[Asset]]
      }
      .recover { case err: Throwable =>
        throw new RuntimeException(s"fetchChanges error, url: ${req.url}", err)
      }
  }
}

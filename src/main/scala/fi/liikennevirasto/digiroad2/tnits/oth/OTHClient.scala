package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.function.Function

import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import org.asynchttpclient
import org.asynchttpclient.{DefaultAsyncHttpClient, Realm, Response, proxy}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats

  private val changesApiUrl = config.urls.changesApi


  def fetchChangesWithAsyncHttpClient(apiEndpoint: String, since: Instant, until: Instant): CompletableFuture[Seq[Asset]] = {
    val client =
      if (config.optionalProxy.isDefined) {
        val p = config.optionalProxy.get
        val clientConfig = new asynchttpclient.DefaultAsyncHttpClientConfig.Builder()
          .setProxyServer(new proxy.ProxyServer.Builder(p.host, p.port).setRealm(new Realm.Builder(p.username, p.password).setScheme(Realm.AuthScheme.BASIC)))
          .build()
        new DefaultAsyncHttpClient(clientConfig)
      } else {
        new DefaultAsyncHttpClient
      }

    val url =
      s"$changesApiUrl/$apiEndpoint?since=$since&until=$until"

    println(s"*** URL: $url")

    val responseFuture = client
      .prepareGet(url)
      .setRealm(new Realm.Builder(config.logins.oth.username, config.logins.oth.password).build())
      .execute()

    println("a")

    val completableFuture = responseFuture
      .toCompletableFuture

    println("b")

    completableFuture
      .thenApplyAsync(new Function[Response, Seq[Asset]] {
        override def apply(response: Response): Seq[Asset] = {
          println("inside")
          (parse(response.getResponseBody) \ "features").extract[Seq[Asset]]: Seq[Asset]
        }
      })
  }
}

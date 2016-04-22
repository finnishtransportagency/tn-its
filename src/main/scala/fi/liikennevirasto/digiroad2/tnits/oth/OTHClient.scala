package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant

import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import org.asynchttpclient
import org.asynchttpclient._
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration._


object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats

  private val changesApiUrl = config.urls.changesApi


  def fetchChangesWithAsyncHttpClient(apiEndpoint: String, since: Instant, until: Instant): Future[Seq[Asset]] = {
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


    Future {
      val promise = Promise[Response]()

      client
        .prepareGet(url)
        .setRealm(new Realm.Builder(config.logins.oth.username, config.logins.oth.password).build())
        .execute(new AsyncCompletionHandler[Response] {
          override def onCompleted(response: Response): Response = {
            println("Completed")
            promise.success(response)
            response
          }
        })

      Await.result(promise.future, 30.seconds)
    }
      .map { response =>
        println("inside")
        (parse(response.getResponseBody) \ "features").extract[Seq[Asset]]: Seq[Asset]
      }
  }
}

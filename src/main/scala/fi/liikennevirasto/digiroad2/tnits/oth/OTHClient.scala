package fi.liikennevirasto.digiroad2.tnits.oth

import java.net.URI
import java.time.Instant

import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.language.reflectiveCalls

/**
  * Provides HTTP access to OTH change api
  */
object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats
  private val client = createClient
  private val target = {
    val changesApiUri = URI.create(config.urls.changesApi)
    val port = if (changesApiUri.getPort == -1) { if (changesApiUri.getScheme == "http") 80 else 443 } else { changesApiUri.getPort }
    new HttpHost(changesApiUri.getHost, port, changesApiUri.getScheme)
  }

  /**
    * Returns a Future that will contain changes as [[Asset]]s
    * from given OTH change api endpoint upon successful completion
    */
  def fetchChanges(apiEndpoint: String, since: Instant, until: Instant, executionContext: ExecutionContext): scala.concurrent.Future[Seq[Asset]] = {
    val changesApiUri = URI.create(config.urls.changesApi + "/" + apiEndpoint)

    val get = new HttpGet(changesApiUri.getPath + s"?since=$since&until=$until")
    get.setConfig(config.optionalProxy.fold(RequestConfig.DEFAULT) { proxy =>
      val p = new HttpHost(proxy.host, proxy.port)
      RequestConfig.custom()
        .setProxy(p)
        .build()
    })

    Future {
      println(s"Fetch: ${get.getURI}")

      using(client.execute(target, get)) { response =>
        val contents = response.getEntity.getContent

        val s = Source.fromInputStream(contents).mkString

        println(s"[$apiEndpoint] Response: $s")

        if (s.nonEmpty && response.getStatusLine.getStatusCode == 200) {
          val parsed = (parse(s) \ "features").extract[Seq[Asset]]
          println(s"[$apiEndpoint] Parsed ${parsed.size} assets")
          parsed
        } else {
          throw new RuntimeException(s"""
                ************************* OTH Request failed *************************
                Request: ${target.toURI}${get.getURI}
                Status: ${response.getStatusLine}
                Response body:
                ${s}
                ****************** End of failed OTH Request **************************""".stripMargin)
        }
      }
    }(executionContext)
  }

  private def using[T <: AutoCloseable, R](resource: T)(f: T => R) =
    try {
      f(resource)
    } finally
      resource.close()

  private def createClient = {
    val credentialsProvider = new BasicCredentialsProvider
    config.optionalProxy.foreach { proxy =>
      credentialsProvider.setCredentials(
        new AuthScope(proxy.host, proxy.port),
        new UsernamePasswordCredentials(proxy.username, proxy.password))
    }
    val changesApiUri = URI.create(config.urls.changesApi)
    credentialsProvider.setCredentials(
      new AuthScope(changesApiUri.getHost, changesApiUri.getPort),
      new UsernamePasswordCredentials(config.logins.oth.username, config.logins.oth.password))

    HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).setMaxConnPerRoute(10).build()
  }
}

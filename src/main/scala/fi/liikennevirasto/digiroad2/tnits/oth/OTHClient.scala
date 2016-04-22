package fi.liikennevirasto.digiroad2.tnits.oth

import java.net.URI
import java.time.Instant
import java.util.concurrent.Executors

import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicCredentialsProvider, HttpClients}
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

import scala.concurrent.ExecutionContext
import scala.io.Source

object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats
  private val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
  private val client = createClient

  def fetchChanges(apiEndpoint: String, since: Instant, until: Instant): scala.concurrent.Future[Seq[Asset]] = {
    val changesApiUri = URI.create(config.urls.changesApi + "/" + apiEndpoint)

    val port = if (changesApiUri.getPort == -1) { if (changesApiUri.getScheme == "http") 80 else 443 } else { changesApiUri.getPort }
    val target = new HttpHost(changesApiUri.getHost, port, changesApiUri.getScheme)

    val get = new HttpGet(changesApiUri.getPath + s"?since=$since&until=$until")
    get.setConfig(config.optionalProxy.fold(RequestConfig.DEFAULT) { proxy =>
      val p = new HttpHost(proxy.host, proxy.port)
      RequestConfig.custom()
        .setProxy(p)
        .build()
    })

    scala.concurrent.Future {
      println(s"Fetch: ${get.getURI}")

      using(client.execute(target, get)) { response =>
        val response = client.execute(target, get)
        val contents = response.getEntity.getContent

        val s = Source.fromInputStream(contents).mkString

        println(s"Response: $s")

        (parse(s) \ "features").extract[Seq[Asset]]
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

    HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).setMaxConnPerRoute(20).build()
  }

}

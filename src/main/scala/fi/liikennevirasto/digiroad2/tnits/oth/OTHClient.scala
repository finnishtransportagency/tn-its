package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant

import com.ning.http.client.ProxyServer
import com.ning.http.client.ProxyServer.Protocol
import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
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

    config.optionalProxy.fold(req) { proxy =>
      req.setProxyServer(new ProxyServer(Protocol.HTTP, proxy.host, proxy.port, proxy.username, proxy.password))
    }
  }

  def fetchChanges(apiEndpoint: String, since: Instant, until: Instant): Future[Seq[Asset]] = {
    val req = (changesApiUrl / apiEndpoint)
      .addQueryParameter("since", since.toString)
      .addQueryParameter("until", until.toString)

    Http(req OK as.String)
      .map { contents =>
        (parse(contents) \ "features").extract[Seq[Asset]]
      }
  }
}

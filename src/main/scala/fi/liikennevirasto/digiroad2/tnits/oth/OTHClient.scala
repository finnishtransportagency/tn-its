package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant

import dispatch._
import dispatch.Defaults._
import fi.liikennevirasto.digiroad2.tnits.rosatte.features
import fi.liikennevirasto.digiroad2.tnits.config
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.JsonMethods._

import scala.concurrent.Await
import scala.concurrent.duration._

object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats

  private val changesApiUrl: Req =
    host(config.urls('changes))
      .setFollowRedirects(true)
      .as(
        user = config.logins('oth).username,
        password = config.logins('oth).password)

  def readSpeedLimitChanges(since: Instant, until: Instant): Seq[features.Asset] = {
    val req = (changesApiUrl / "speed_limits")
      .addQueryParameter("since", since.toString)
      .addQueryParameter("until", until.toString)
    val contents = Await.result(Http(req OK as.String), 30.seconds)
    (parse(contents) \ "features").extract[Seq[features.Asset]]
  }
}

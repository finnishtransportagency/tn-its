package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant

import dispatch._
import dispatch.Defaults._
import fi.liikennevirasto.digiroad2.tnits.rosatte.features
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
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
    fetchChanges("speed_limits", since, until)
  }

  def readTotalWeightLimitChanges(since: Instant, until: Instant): Seq[features.Asset] = {
    fetchChanges("total_weight_limits", since, until)
  }

  private def fetchChanges(apiEndpoint: String, since: Instant, until: Instant): Seq[Asset] = {
    val req = (changesApiUrl / apiEndpoint)
      .addQueryParameter("since", since.toString)
      .addQueryParameter("until", until.toString)
    val contents = Await.result(Http(req OK as.String), 30.seconds)
    (parse(contents) \ "features").extract[Seq[Asset]]
  }
}

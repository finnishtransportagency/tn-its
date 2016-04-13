package fi.liikennevirasto.digiroad2.tnits.oth

import java.time.Instant

import dispatch.Defaults._
import dispatch._
import fi.liikennevirasto.digiroad2.tnits.config
import fi.liikennevirasto.digiroad2.tnits.rosatte.features.Asset
import org.json4s.jackson.JsonMethods._
import org.json4s.{DefaultFormats, Formats}

object OTHClient {
  protected implicit val jsonFormats: Formats = DefaultFormats

  private val changesApiUrl: Req =
    host(config.urls('changes))
      .setFollowRedirects(true)
      .as(
        user = config.logins('oth).username,
        password = config.logins('oth).password)

  def readSpeedLimitChanges(since: Instant, until: Instant): Future[Seq[Asset]] = {
    fetchChanges("speed_limits", since, until)
  }

  def readTotalWeightLimitChanges(since: Instant, until: Instant): Future[Seq[Asset]] = {
    fetchChanges("total_weight_limits", since, until)
  }

  def fetchChanges(apiEndpoint: String, since: Instant, until: Instant): Future[Seq[Asset]] = {
    val req = (changesApiUrl / apiEndpoint)
      .addQueryParameter("since", since.toString)
      .addQueryParameter("until", until.toString)
    Http(req OK as.String).map(contents => (parse(contents) \ "features").extract[Seq[Asset]])
  }
}

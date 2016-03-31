package fi.liikennevirasto.digiroad2.tnits

import dispatch.Defaults._
import dispatch._

import scala.concurrent.Await
import scala.concurrent.duration._

object RosatteConverter {

  private val changesApiUrl: Req =
    (host("localhost:6666") / "digiroad" / "api" / "changes")
      .setFollowRedirects(true)
      .as(
        user = sys.env.getOrElse("CHANGE_API_USERNAME", ""),
        password = sys.env.getOrElse("CHANGE_API_PASSWORD", ""))

  def main(args: Array[String]) {
    val req = (changesApiUrl / "speed_limits").addQueryParameter("since", "2016-03-01T16:00")
    val contents = Await.result(Http(req OK as.String), 30.seconds)
    println(contents)
  }

}

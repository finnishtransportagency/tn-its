package fi.liikennevirasto.digiroad2.tnits

import java.net.{URI, URL}

object config {
  case class Login(
    username: String,
    password: String)

  val logins = new {
    val aineistot = new {
      val username = env("AINEISTOT_USERNAME")
      val password = env("AINEISTOT_PASSWORD")
    }
    val oth = new {
      val username = env("CHANGE_API_USERNAME")
      val password = env("CHANGE_API_PASSWORD")
    }
  }

  val dir = env("AINEISTOT_DIRECTORY")

  val urls = new {
    val aineistot = new {
      val base = "aineistot.liikennevirasto.fi"
      val dir = s"http://$base/digiroad/${config.dir}"
      val ftp = base
    }
    val changesApi = env("CHANGE_API_URL")
  }

  val proxy = getProxy

  val apiPort =
    sys.env.get("PORT").fold(8090)(_.toInt)

  private def env(name: String) =
    sys.env.getOrElse(name, sys.error(s"Environment variable required: $name"))

  private def getProxy = {
    val url = URI.create(env("QUOTAGUARDSTATIC_URL"))
    val Array(user, pass) = url.getUserInfo.split(":")

    new {
      val host = url.getHost
      val port = url.getPort
      val username = user
      val password = pass
    }
  }
}

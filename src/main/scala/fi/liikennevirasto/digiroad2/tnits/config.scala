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
      val domain = "aineistot.liikennevirasto.fi"
      val dir = s"http://$domain/digiroad/${config.dir}"
      val ftp = domain
    }
    val changesApi = env("CHANGE_API_URL")
  }

  val optionalProxy = getProxy

  val apiPort =
    sys.env.get("PORT").fold(8090)(_.toInt)

  private def env(name: String) =
    sys.env.getOrElse(name, sys.error(s"Environment variable required: $name"))

  private def optionalEnv(name: String): Option[String] =
    sys.env.get(name)

  private def getProxy = {
    optionalEnv("QUOTAGUARDSTATIC_URL").map { quotaGuardStaticUrl =>
      val url = URI.create(quotaGuardStaticUrl)
      val Array(user, pass) = url.getUserInfo.split(":")

      new {
        val host = url.getHost
        val port = url.getPort
        val username = user
        val password = pass
      }
    }
  }
}

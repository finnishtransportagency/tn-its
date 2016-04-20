package fi.liikennevirasto.digiroad2.tnits

import java.net.{URI, URL}

object config {
  val logins = new {
    val aineistot = new {
      val username = env("AINEISTOT_USERNAME")
      val password = env("AINEISTOT_PASSWORD")
    }
    val oth = new {
      val username = env("CHANGE_API_USERNAME")
      val password = env("CHANGE_API_PASSWORD")
    }
    val converter = new {
      val username = env("CONVERTER_API_USERNAME")
      val password = env("CONVERTER_API_PASSWORD")
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

  val apiPort = optionalEnv("PORT").fold(8090)(_.toInt)

  private def getProxy = {
    optionalEnv("PROXIMO_URL")
      .map(URI.create)
      .map { url =>
        val Array(user, pass) = url.getUserInfo.split(":")
        val parsedPort = if (url.getPort == -1) {
          if (url.getScheme == "https") 443 else 80
        } else {
          url.getPort
        }

        println(s"Proxy: ${url.getHost} $parsedPort $user $pass")

        new {
          val host = url.getHost
          val port = parsedPort
          val username = user
          val password = pass
        }
      }
  }

  private def env(name: String) =
    sys.env.getOrElse(name, sys.error(s"Environment variable required: $name"))

  private def optionalEnv(name: String): Option[String] =
    sys.env.get(name)
}

package fi.liikennevirasto.digiroad2.tnits

import java.net.URI

/** Configuration values that are read at startup from the environment. */
object config {
  val logins = new {
    val oth = new {
      val username = env("CHANGE_API_USERNAME")
      val password = env("CHANGE_API_PASSWORD")
    }
    val converter = new {
      val username = env("CONVERTER_API_USERNAME")
      val password = env("CONVERTER_API_PASSWORD")
    }
    val aineistotSFTP = new {
      val username = env("AINEISTOT_SFTP_USERNAME")
      val password = env("AINEISTOT_SFTP_PASSWORD")
    }
  }

  val dirSFTP = env("AINEISTOT_SFTP_DIRECTORY")
  val baseDirSFTP = env("AINEISTOT_SFTP_BASE_DIRECTORY") + "/" + env("AINEISTOT_SFTP_DIRECTORY")
  val dirBusStopsSFTP = env("AINEISTOT_SFTP_BUS_STOPS_DIRECTORY")
  val baseDirBusStopsSFTP = env("AINEISTOT_SFTP_BASE_DIRECTORY") + "/" + env("AINEISTOT_SFTP_BUS_STOPS_DIRECTORY")

  val urls = new {
    val aineistotSFTP = new {
      val domain = env("AINEISTOT_SFTP_DOMAIN")
      val dir = s"https://$domain/${config.dirSFTP}"
      val sftp = domain
    }
    val aineistotSFTPForBusStops = new {
      val domain = env("AINEISTOT_SFTP_DOMAIN")
      val dir = s"https://$domain/${config.dirBusStopsSFTP}"
      val sftp = domain
    }
    val changesApi = env("CHANGE_API_URL")
    val viiteChangeApi = env("VIITE_CHANGE_API_URL")
  }

  val optionalProxy = getProxy

  val apiPort = optionalEnv("PORT").fold(8090)(_.toInt)
  val sftpServerPort = optionalEnv("PORT_SFTP").fold(22)(_.toInt)

  private def getProxy = {
    optionalEnv("QUOTAGUARDSTATIC_URL")
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
          override def toString = s"$host $port $username $password"
        }
      }
  }

  private def env(name: String) =
    sys.env.getOrElse(name, sys.error(s"Environment variable required: $name"))

  private def optionalEnv(name: String): Option[String] =
    sys.env.get(name).filter(_ != null)
}

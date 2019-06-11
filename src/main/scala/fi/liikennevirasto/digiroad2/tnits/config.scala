package fi.liikennevirasto.digiroad2.tnits

import java.net.URI

/** Configuration values that are read at startup from the environment. */
object config {

  val variables = Map(
    "AINEISTOT_USERNAME" -> "tn_its",
    "AINEISTOT_PASSWORD" -> "Ki10!aD10!as",
    "AINEISTOT_DIRECTORY" -> "",
    "AINEISTOT_SFTP_DIRECTORY" -> "tnits",
    "AINEISTOT_SFTP_BUS_STOPS_DIRECTORY" -> "digiroad/muutokset_pysakit_xml",
    "AINEISTOT_SFTP_WEIGTH_LIMIT_DIRECTORY" -> "tnits/droth-1987-tests", //TODO
    "AINEISTOT_SFTP_USERNAME" -> "tn_its",
    "AINEISTOT_SFTP_PASSWORD" -> "Kissa10!as400!rW2",
    "AINEISTOT_SFTP_BASE_DIRECTORY" -> "aineistot.liikennevirasto.fi",
    "AINEISTOT_SFTP_DOMAIN" -> "aineistot.vayla.fi",
    "CHANGE_API_USERNAME" -> "kalpa",
    "CHANGE_API_PASSWORD" -> "kalpa",
    "CHANGE_API_URL" -> "http://localhost:9001/api/changes",
    "VIITE_CHANGE_API_URL" -> "https://devtest.vayla.fi/viite/api/viite/changes",
    "CONVERTER_API_USERNAME" -> "",
    "CONVERTER_API_PASSWORD" -> ""
  )


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
  val dirWeightLimitSFTP = env("AINEISTOT_SFTP_BASE_DIRECTORY")
  val baseWeightLimitSFTP = env("AINEISTOT_SFTP_BASE_DIRECTORY") + "/" + env("AINEISTOT_SFTP_WEIGTH_LIMIT_DIRECTORY")

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
    val aineistotSFTPForWeigthLimits = new {
      val domain = env("AINEISTOT_SFTP_DOMAIN")
      val dir = s"https://$domain/${config.dirWeightLimitSFTP}"
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

  private def env(name: String) = {
    //sys.env.getOrElse(name, sys.error(s"Environment variable required: $name"))
    variables.getOrElse(name, sys.error(s"Environment variable required: $name"))
  }

  private def optionalEnv(name: String): Option[String] = {
    variables.get(name).filter(_ != null)
    //sys.env.get(name).filter(_ != null)
  }
}

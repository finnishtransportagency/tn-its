package fi.liikennevirasto.digiroad2.tnits

object config {
  case class Login(
    username: String,
    password: String)

  val logins = Map(
    'aineistot -> Login(
      username = env("AINEISTOT_USERNAME"),
      password = env("AINEISTOT_PASSWORD")),
    'oth -> Login(
      username = env("CHANGE_API_USERNAME"),
      password = env("CHANGE_API_PASSWORD")))

  val aineistot = "aineistot.liikennevirasto.fi"
  val dir = env("AINEISTOT_DIRECTORY")

  val urls = Map(
    'aineistot -> s"http://$aineistot/digiroad/$dir",
    'aineistotFtp -> aineistot,
    'changes -> env("CHANGE_API_URL")
  )

  val apiPort =
    sys.env.get("PORT").fold(8090)(_.toInt)

  private def env(name: String) =
    sys.env.getOrElse(name, sys.error(s"Environment variable required: $name"))
}

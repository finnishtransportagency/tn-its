package fi.liikennevirasto.digiroad2.tnits

object config {
  case class Login(
    username: String,
    password: String)

  val logins: Map[Symbol, Login] = Map(
    'aineistot -> Login(
      username = sys.env.getOrElse("AINEISTOT_USERNAME", ""),
      password = sys.env.getOrElse("AINEISTOT_PASSWORD", "")),
    'oth -> Login(
      username = sys.env.getOrElse("CHANGE_API_USERNAME", ""),
      password = sys.env.getOrElse("CHANGE_API_PASSWORD", "")
    )
  )

  val urls = Map(
    'aineistot -> "http://aineistot.liikennevirasto.fi/digiroad/tnits-testdata",
    'aineistotFtp -> "aineistot.liikennevirasto.fi",
    'changes -> sys.env.getOrElse("CHANGE_API_URL", "")
  )

  val apiPort =
    sys.env.get("PORT").fold(8090)(_.toInt)
}

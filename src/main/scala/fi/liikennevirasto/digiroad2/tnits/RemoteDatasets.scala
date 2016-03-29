package fi.liikennevirasto.digiroad2.tnits

import dispatch._, Defaults._

object RemoteDatasets {
  private val indexUrl =
    url("http://aineistot.liikennevirasto.fi/digiroad/tnits-testdata/")
      .as(
        user = sys.env.getOrElse("AINEISTOT_USERNAME", ""),
        password = sys.env.getOrElse("AINEISTOT_PASSWORD", ""))

  def index: Future[String] =
    Http(indexUrl OK as.String)
}

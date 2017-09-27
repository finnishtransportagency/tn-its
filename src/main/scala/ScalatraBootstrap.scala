import javax.servlet.ServletContext

import fi.liikennevirasto.digiroad2.tnits.api.{ConversionApi, Digiroad2TnItsApi}
import org.scalatra._
import fi.liikennevirasto.digiroad2.tnits.config

/** Declares HTTP API paths and classes. */
class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    println(s"Listening on ${config.apiPortSFTP}")
    context.mount(new Digiroad2TnItsApi, "/rosattedownload/*")
    context.mount(new ConversionApi, "/conversion/*")
  }
}

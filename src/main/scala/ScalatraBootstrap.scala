import javax.servlet.ServletContext

import fi.liikennevirasto.digiroad2.tnits.api.Digiroad2TnItsApi
import org.scalatra._
import fi.liikennevirasto.digiroad2.tnits.config

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    println(s"Listening on ${config.apiPort}")
    context.mount(new Digiroad2TnItsApi, "/*")
  }
}

import javax.servlet.ServletContext

import fi.liikennevirasto.digiroad2.tnits.api.Digiroad2TnItsApi
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new Digiroad2TnItsApi, "/*")
  }
}

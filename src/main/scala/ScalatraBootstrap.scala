import fi.liikennevirasto.digiroad2.tnits._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.setInitParameter(ScalatraBase.PortKey, "8091")
    context.mount(new Digiroad2TnItsApi, "/*")
  }
}

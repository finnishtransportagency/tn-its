package fi.liikennevirasto.digiroad2.tnits.runners

import fi.liikennevirasto.digiroad2.tnits.config
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

// Read src/main/scala/ScalatraBootstrap.scala to see the Api listeners. It's not here
// because of Scalatra reasons.
object Api {
  def main(args: Array[String]) {
    val server = new Server(config.apiPort)
    val context = new WebAppContext()
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")

    context.setEventListeners(Array(new ScalatraListener))

    server.setHandler(context)

    server.start()
    server.join()
  }
}

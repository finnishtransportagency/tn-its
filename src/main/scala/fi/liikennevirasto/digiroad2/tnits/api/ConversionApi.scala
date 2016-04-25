package fi.liikennevirasto.digiroad2.tnits.api

import java.io.{PrintWriter, OutputStream}
import fi.liikennevirasto.digiroad2.tnits.runners.Converter
import org.scalatra._

import scala.concurrent.ExecutionContext

class ConversionApi extends ScalatraServlet with FutureSupport with AuthenticationSupport {

  override protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.global

  before() {
    basicAuth
  }

  post("/") {
    val writer = response.writer.
    val keepAlive = keepConnectionAlive(writer)
    Converter.convert(writer)
    keepAlive.cancel()
    Unit
  }

  def keepConnectionAlive(writer: PrintWriter) = {
    val timer = new java.util.Timer()
    val task = new java.util.TimerTask {
      def run() = {
        writer.println("*** keep alive ***")
        writer.flush()
      }
    }
    timer.schedule(task, 10000L, 10000L)
    timer
  }
}

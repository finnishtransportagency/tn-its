package fi.liikennevirasto.digiroad2.tnits.api

import fi.liikennevirasto.digiroad2.tnits.runners.Converter
import org.scalatra._

import scala.concurrent.ExecutionContext

class ConversionApi extends ScalatraServlet with FutureSupport {

  override protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.global

  post("/") {
    Converter.main(Array())
    "OK"
  }

}

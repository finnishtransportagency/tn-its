package fi.liikennevirasto.digiroad2.tnits.api

import java.io.{OutputStream, PrintWriter}
import java.time.Instant
import java.time.temporal.ChronoUnit

import fi.liikennevirasto.digiroad2.tnits.aineistot.RemoteDatasets
import fi.liikennevirasto.digiroad2.tnits.runners.{BaseAssetConverter, BusStopConverter, Converter, WeightLimitConverter}
import org.scalatra._

import scala.concurrent.ExecutionContext

/** Provides optional way to start the conversion process generating the next Rosatte formatted change data set.
  *
  * The conversion process can also run from command line, see: [[Converter]].
  */
class ConversionApi extends ScalatraServlet with FutureSupport with AuthenticationSupport {

  lazy val baseAssetConverter = new BaseAssetConverter
  lazy val busStopAssetConverter = new BusStopConverter
  lazy val weightLimitAssetConverter = new WeightLimitConverter

  override protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.global

  before() {
    basicAuth
  }

  post("/:endDate") {
    val startDate = RemoteDatasets.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS))
    val endDate = Instant.parse(params("endDate"))

    if(startDate.isAfter(endDate)  || endDate.isAfter(Instant.now))
      halt(BadRequest(s"Wrong date period startDate - $startDate / endDate - $endDate "))

    val numberOfDays = startDate.until(endDate, ChronoUnit.DAYS)

    val writer = response.writer
    val keepAlive = keepConnectionAlive(writer)

    if(numberOfDays == 0 ) {
      println("Running in manually mode, only some hours")
      baseAssetConverter.convert(writer, Some(startDate), Some(endDate))
      weightLimitAssetConverter.convert(writer, Some(startDate), Some(endDate))
    }

    for (counter <- 1 to numberOfDays.toInt) {
      baseAssetConverter.convert(writer, Some(startDate.plus(counter - 1, ChronoUnit.DAYS)), Some(startDate.plus(counter, ChronoUnit.DAYS)))
      weightLimitAssetConverter.convert(writer, Some(startDate.plus(counter - 1, ChronoUnit.DAYS)), Some(startDate.plus(counter, ChronoUnit.DAYS)))
    }

    keepAlive.cancel()
    writer.println("OK")
    writer.flush()
    Unit
  }

  post("/") {
    val writer = response.writer
    val keepAlive = keepConnectionAlive(writer)
    baseAssetConverter.convert(writer)
    weightLimitAssetConverter.convert(writer)
    keepAlive.cancel()
    writer.println("OK")
    writer.flush()
    Unit
  }

  post("/mass_transit_stop_on_vallu") {
    val writer = response.writer
    val keepAlive = keepConnectionAlive(writer)
    busStopAssetConverter.convert(writer)
    keepAlive.cancel()
    writer.println("OK")
    writer.flush()
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

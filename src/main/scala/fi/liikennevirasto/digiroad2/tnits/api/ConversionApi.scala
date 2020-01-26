package fi.liikennevirasto.digiroad2.tnits.api

import java.io.PrintWriter
import java.time.Instant
import java.time.temporal.ChronoUnit

import fi.liikennevirasto.digiroad2.tnits.runners.{BusStopConverter, Converter, NonStdConverter, StdConverter}
import org.scalatra._

import scala.concurrent.ExecutionContext

/** Provides optional way to start the conversion process generating the next Rosatte formatted change data set.
  *
  * The conversion process can also run from command line, see: [[Converter]].
  */
class ConversionApi extends ScalatraServlet with FutureSupport with AuthenticationSupport {

  lazy val stdConverter = new StdConverter
  lazy val busStopAssetConverter = new BusStopConverter
  lazy val nonStdConverter = new NonStdConverter

  override protected implicit def executor: ExecutionContext =
    scala.concurrent.ExecutionContext.global

  before() {
    basicAuth
  }


def manual(converter: Converter, writer: PrintWriter): Unit = {
  val startDate = converter.getLatestEndTime.getOrElse(Instant.now.minus(1, ChronoUnit.DAYS))
  val endDate = Instant.parse(params("endDate"))

  if(startDate.isAfter(endDate)  || endDate.isAfter(Instant.now))
    halt(BadRequest(s"Wrong date period startDate - $startDate / endDate - $endDate "))

  val numberOfDays = startDate.until(endDate, ChronoUnit.DAYS)

  for (counter <- 1 to numberOfDays.toInt) {
    writer.println(s"***** Convert *****")
    converter.convert(writer, Some(startDate.plus(counter - 1, ChronoUnit.DAYS)), Some(startDate.plus(counter, ChronoUnit.DAYS)))
  }
}

  post("/:endDate") {
    val writer = response.writer
    val keepAlive = keepConnectionAlive(writer)

    writer.println(s"***** Standard Asset Converter *****")
    manual(stdConverter, writer)

    keepAlive.cancel()
    writer.println("OK")
    writer.flush()


    val writerNonStd = response.writer
    val keepAliveNonStd = keepConnectionAlive(writerNonStd)

    writerNonStd.println(s"***** NonStandard Asset Converter *****")
    manual(nonStdConverter, writerNonStd)
    keepAliveNonStd.cancel()
    writerNonStd.println("OK")
    writerNonStd.flush()
    Unit
  }

  post("/") {
    val writer = response.writer
    val keepAlive = keepConnectionAlive(writer)
    writer.println(s"***** Standard Asset Converter *****")
    stdConverter.convert(writer)
    writer.println(s"***** NonStandard Asset Converter *****")
    nonStdConverter.convert(writer)
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

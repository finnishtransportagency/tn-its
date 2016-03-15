package fi.liikennevirasto.digiroad2.tnits.examples

import java.util.Base64

import openlr.binary.ByteArray
import openlr.encoder.{OpenLREncoder, OpenLREncoderParameter}
import openlr.location.LocationFactory
import openlr.map.loader.MapLoadParameter
import openlr.map.sqlite.loader.{DBFileNameParameter, SQLiteMapLoader}

object OpenLRTest {

  def main(args: Array[String]) = {
    import collection.JavaConverters._

    val mapLoader = new SQLiteMapLoader
    val dbFileName = new DBFileNameParameter()
    dbFileName.setValue("tomtom-openlr-testdata-utrecht/tomtom_utrecht_2008_04_copy.db3")
    val mapDatabase = mapLoader.load(Seq[MapLoadParameter](dbFileName).asJava)
    val encoder = new OpenLREncoder
    val param = new OpenLREncoderParameter.Builder()
      .`with`(mapDatabase)
      .buildParameter()
    val lines = Seq(mapDatabase.getLine(15280002805007L)).asJava
    val lineLocation = LocationFactory.createLineLocation("loc", lines)
    val encoded = encoder.encodeLocation(param, lineLocation)
    val reference = encoded.getLocationReference("binary")
    val data = reference.getLocationReferenceData.asInstanceOf[ByteArray]
    println(new String(Base64.getEncoder.encode(data.getData), "ASCII"))
  }

}

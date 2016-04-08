package fi.liikennevirasto.digiroad2.tnits.geometry

import org.geotools.referencing.CRS

object CoordinateTransform {
  def convertToWgs84(coordinates: Seq[Double]): Seq[Double] = {
    val OTHReferencingSystem = CRS.decode("EPSG:3067")
    val openLRReferencingSystem = CRS.decode("EPSG:4326")
    val transformation = CRS.findMathTransform(OTHReferencingSystem, openLRReferencingSystem)
    val transformedCoordinates = new Array[Double](coordinates.length)
    transformation.transform(coordinates.toArray, 0, transformedCoordinates, 0, coordinates.length / 2)
    transformedCoordinates
  }
}

package fi.liikennevirasto.digiroad2.tnits.geojson

/**
  * GeoJSON LineString
  *
  * See: http://geojson.org/geojson-spec.html#linestring
  */
case class LineString(coordinates: Seq[Seq[Double]])

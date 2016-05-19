package fi.liikennevirasto.digiroad2.tnits.geojson

/** GeoJSON LineString
  *
  * @see [[http://geojson.org/geojson-spec.html#linestring]]
  */
case class LineString(coordinates: Seq[Seq[Double]])

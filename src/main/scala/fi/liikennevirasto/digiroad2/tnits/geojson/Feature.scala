package fi.liikennevirasto.digiroad2.tnits.geojson

/** GeoJSON feature.
  *
  * @see [[http://geojson.org/geojson-spec.html#feature-objects]]
  */

trait Feature[Properties]{
  val id: Long
  val properties: Properties
}

case class FeatureLinear[Properties](id: Long, geometry: LineString, properties: Properties) extends Feature[Properties]

case class FeaturePoint[Properties](id: Long, geometry: Point, properties: Properties) extends Feature[Properties]


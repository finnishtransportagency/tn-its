package fi.liikennevirasto.digiroad2.tnits.geojson

/**
  * GeoJSON feature
  *
  * see http://geojson.org/geojson-spec.html#feature-objects
  */
case class Feature[Properties](id: Long, geometry: LineString, properties: Properties)

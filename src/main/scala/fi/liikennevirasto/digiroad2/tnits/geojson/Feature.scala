package fi.liikennevirasto.digiroad2.tnits.geojson

case class Feature[Properties](id: Long, geometry: LineString, properties: Properties)

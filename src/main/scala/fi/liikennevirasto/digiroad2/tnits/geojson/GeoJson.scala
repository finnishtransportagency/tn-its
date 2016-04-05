package fi.liikennevirasto.digiroad2.tnits.geojson

case class Feature[Properties](geometry: LineString, properties: Properties)
case class LineString(coordinates: Seq[Seq[Double]])

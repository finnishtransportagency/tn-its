package fi.liikennevirasto.digiroad2.tnits.geojson

case class Feature(geometry: LineString, properties: Map[String, Any])
case class LineString(coordinates: Seq[Seq[Double]])

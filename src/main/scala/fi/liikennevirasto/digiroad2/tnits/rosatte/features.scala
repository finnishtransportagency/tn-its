package fi.liikennevirasto.digiroad2.tnits.rosatte

import fi.liikennevirasto.digiroad2.tnits.geojson

object features {
  type Asset = geojson.Feature[AssetProperties]
  type RoadLink = geojson.Feature[RoadLinkProperties]

  case class AssetProperties(
    sideCode: Int,
    changeType: String,
    value: Int,
    startMeasure: Double,
    endMeasure: Double,
    id: Long,
    link: RoadLink)

  case class RoadLinkProperties(
    id: Long,
    functionalClass: Int,
    `type`: Int,
    length: Double)
}

package fi.liikennevirasto.digiroad2.tnits.rosatte

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID.DataSetId
import org.scalatest.FunSuite

class RosatteSpec extends FunSuite {
  test("Dataset IDs should rountrip") {
    val uuid = UUID.randomUUID()
    val start = Instant.now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)
    val end = Instant.now.truncatedTo(ChronoUnit.SECONDS)

    val encodedDataSetId = DatasetID.encode(uuid, start, end)
    val DataSetId(decodedUuid, decodedStart, decodedEnd) = DatasetID.decode(encodedDataSetId)

    assert(decodedUuid == uuid)
    assert(decodedStart == start)
    assert(decodedEnd == end)
  }
}
package fi.liikennevirasto.digiroad2.tnits

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.Rosatte.DataSetId
import org.specs2.mutable.Specification

class RosatteSpec extends Specification {
  "Data set ID values should roundtrip" >> {
    val uuid = UUID.randomUUID()
    val start = Instant.now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)
    val end = Instant.now.truncatedTo(ChronoUnit.SECONDS)

    val encodedDataSetId = Rosatte.encodeDataSetId(uuid, start, end)
    val DataSetId(decodedUuid, decodedStart, decodedEnd) = Rosatte.decodeDataSetId(encodedDataSetId)

    decodedUuid must_== uuid
    decodedStart must_== start
    decodedEnd must_== end
  }
}
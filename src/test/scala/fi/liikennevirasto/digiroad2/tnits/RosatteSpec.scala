package fi.liikennevirasto.digiroad2.tnits

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID
import fi.liikennevirasto.digiroad2.tnits.rosatte.DatasetID.DataSetId
import org.specs2.mutable.Specification

class RosatteSpec extends Specification {
  "Data set ID values should roundtrip" >> {
    val uuid = UUID.randomUUID()
    val start = Instant.now.minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)
    val end = Instant.now.truncatedTo(ChronoUnit.SECONDS)

    val encodedDataSetId = DatasetID.encode(uuid, start, end)
    val DataSetId(decodedUuid, decodedStart, decodedEnd) = DatasetID.decode(encodedDataSetId)

    decodedUuid must_== uuid
    decodedStart must_== start
    decodedEnd must_== end
  }
}
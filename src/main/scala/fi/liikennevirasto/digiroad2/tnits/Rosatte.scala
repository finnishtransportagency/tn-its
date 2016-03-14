package fi.liikennevirasto.digiroad2.tnits

import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.{Base64, UUID}
import java.time.Instant
import java.time.temporal.ChronoUnit

object Rosatte {
  def encodeDataSetId(issuer: UUID, start: Instant, end: Instant): String = {
    val bytes = ByteBuffer.allocate(32)
    bytes.putLong(issuer.getMostSignificantBits)
    bytes.putLong(issuer.getLeastSignificantBits)
    bytes.putLong(start.getEpochSecond)
    bytes.putLong(end.getEpochSecond)
    bytes.position(0)

    new String(Base64.getEncoder.encode(bytes.array()), "ASCII")
  }

  def decodeDataSetId(id: String): (UUID, Instant, Instant) = {
    val bytes = Base64.getDecoder.decode(id)
    val uuid = new UUID(bytesToLong(bytes.slice(0, 8)), bytesToLong(bytes.slice(8, 16)))
    val start = bytesToLong(bytes.slice(16, 24))
    val end = bytesToLong(bytes.slice(24, 32))

    (uuid, Instant.ofEpochSecond(start), Instant.ofEpochSecond(end))
  }

  private def bytesToLong(bytes: Array[Byte]) =
    new BigInteger(bytes).longValueExact()
}

object TestRosatte {
  def main(args: Array[String]) {
    val uuid = UUID.randomUUID()
    val start = Instant.now.minus(1, ChronoUnit.DAYS)
    val end = Instant.now

    val dataSetId = Rosatte.encodeDataSetId(uuid, start, end)
    println(s"""Rosatte.encodeDataSetId("$uuid", $start, $end) = $dataSetId""")

    val decodedValues = Rosatte.decodeDataSetId(dataSetId)
    println(s"""Rosatte.decode("$dataSetId") = $decodedValues""")
  }
}
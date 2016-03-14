package fi.liikennevirasto.digiroad2.tnits

import java.math.BigInteger
import java.util.{Base64, UUID}
import java.time.Instant

object Rosatte {
  private val base64 = Base64.getDecoder

  def decodeDataSetId(id: String): (UUID, Instant, Instant) = {
    val bytes = base64.decode(id)
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
    print("""Rosatte.decode("1uShiYqi/Ee120s9P1ga7AAAADiDTdVBAAAA3YNN1UE=") = """)
    println(Rosatte.decodeDataSetId("1uShiYqi/Ee120s9P1ga7AAAADiDTdVBAAAA3YNN1UE="))
  }
}
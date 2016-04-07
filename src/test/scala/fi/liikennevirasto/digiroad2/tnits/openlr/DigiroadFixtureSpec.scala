package fi.liikennevirasto.digiroad2.tnits.openlr

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import org.scalatest.FunSuite

/**
  * Tests for DigiroadFixtureMapDatabase, DigiroadLine and DigiroadNode.
  */
class DigiroadFixtureSpec extends FunSuite {
  test("Line start and endpoints") {
    val mapDatabase = DigiroadFixtureMapDatabase(Seq(
      DigiroadLine(1, Seq(Point(0, 0), Point(10, 0)), 10),
      DigiroadLine(2, Seq(Point(10, 0), Point(30, 0)), 20)
    ))

    assert(mapDatabase.getLine(1).getStartNode == DigiroadNode(Point(0, 0)))
    assert(mapDatabase.getLine(1).getEndNode == DigiroadNode(Point(10, 0)))
    assert(mapDatabase.getLine(2).getStartNode == DigiroadNode(Point(10, 0)))
    assert(mapDatabase.getLine(2).getEndNode == DigiroadNode(Point(30, 0)))
  }
}

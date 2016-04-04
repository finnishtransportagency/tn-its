package fi.liikennevirasto.digiroad2.tnits.openlr

import fi.liikennevirasto.digiroad2.tnits.geometry.Point
import org.specs2.mutable.Specification

/**
  * Tests for DigiroadFixtureMapDatabase, DigiroadLine and DigiroadNode.
  */
class DigiroadFixtureSpec extends Specification {
  "Line start and endpoints" >> {
    val mapDatabase = DigiroadFixtureMapDatabase(Seq(
      DigiroadLine(1, Seq(Point(0, 0), Point(10, 0)), 10),
      DigiroadLine(2, Seq(Point(10, 0), Point(30, 0)), 20)
    ))

    mapDatabase.getLine(1).getStartNode must_== DigiroadNode(Point(0, 0))
    mapDatabase.getLine(1).getEndNode must_== DigiroadNode(Point(10, 0))
    mapDatabase.getLine(2).getStartNode must_== DigiroadNode(Point(10, 0))
    mapDatabase.getLine(2).getEndNode must_== DigiroadNode(Point(30, 0))
  }
}

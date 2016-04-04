package fi.liikennevirasto.digiroad2.tnits.geometry

case class Point(x: Double, y: Double, z: Double = 0.0) {
  def distanceTo(point: Point): Double =
    Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2) + Math.pow(point.z - z, 2))

  def -(that: Point): Vector = {
    Vector(x - that.x, y - that.y, z - that.z)
  }

  def -(that: Vector): Point = {
    Point(x - that.x, y - that.y, z - that.z)
  }

  def +(that: Vector): Point = {
    Point(x + that.x, y + that.y, z + that.z)
  }
}

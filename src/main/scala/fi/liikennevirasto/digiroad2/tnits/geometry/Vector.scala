package fi.liikennevirasto.digiroad2.tnits.geometry

case class Vector(x: Double, y: Double, z: Double) {
  def dot(that: Vector): Double = {
    (x * that.x) + (y * that.y) + (z * that.z)
  }

  def normalize(): Vector = {
    scale(1 / length())
  }

  def scale(scalar: Double): Vector = {
    Vector(x * scalar, y * scalar, z * scalar)
  }

  def length(): Double = {
    Math.sqrt((x * x) + (y * y) + (z * z))
  }

  def -(that: Vector): Vector = {
    Vector(x - that.x, y - that.y, z - that.z)
  }
}

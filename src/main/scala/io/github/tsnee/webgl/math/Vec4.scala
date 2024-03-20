package io.github.tsnee.webgl.math

import cats._
import cats.syntax.all._

import scala.annotation.targetName
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

final case class Vec4(private val backingStore: Float32Array)
    extends BackedByFloat32Array:
  rhs =>

  override def toFloat32Array: Float32Array = Float32Array(backingStore)

  def apply(row: Int): Float =
    backingStore.get(row)

  def rotate(degrees: Float, x: Float, y: Float, z: Float): Vec4 =
    Matrix4.setRotate(degrees, x, y, z) * rhs

  def scale(x: Float, y: Float, z: Float): Vec4 =
    Matrix4.setScale(x, y, z) * rhs

  def translate(x: Float, y: Float, z: Float): Vec4 =
    Matrix4.setTranslate(x, y, z) * rhs

  @targetName("minus")
  def -(other: Vec4): Vec4 = Vec4(
    backingStore(0) - other(0),
    backingStore(1) - other(1),
    backingStore(2) - other(2),
    backingStore(3) - other(3)
  )

  def cross(other: Vec4): Vec4 = Vec4(
    backingStore(1) * other(2) - backingStore(2) * other(1),
    backingStore(2) * other(0) - backingStore(0) * other(2),
    backingStore(0) * other(1) - backingStore(1) * other(0),
    0f
  )

  lazy val magnitude: Float = Math.sqrt(
    backingStore(0) * backingStore(0) +
      backingStore(1) * backingStore(1) +
      backingStore(2) * backingStore(2) +
      backingStore(3) * backingStore(3)
  ).toFloat

  lazy val normal: Vec4 = Vec4(
    backingStore(0) / magnitude,
    backingStore(1) / magnitude,
    backingStore(2) / magnitude,
    backingStore(3) / magnitude
  )

object Vec4:
  given eqVec4(using f: Eq[BackedByFloat32Array]): Eq[Vec4] = Eq.instance:
    f.eqv

  def apply(x: Float, y: Float, z: Float, w: Float): Vec4 =
    Vec4(Float32Array(js.Array(x, y, z, w)))

  def point(x: Float, y: Float, z: Float): Vec4 =
    apply(x, y, z, 1f)

  def direction(x: Float, y: Float, z: Float): Vec4 =
    apply(x, y, z, 0f)

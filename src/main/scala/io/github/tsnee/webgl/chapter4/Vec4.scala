package io.github.tsnee.webgl.chapter4

import cats._
import cats.syntax.all._

import scala.annotation.targetName
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

final case class Vec4(private val backingStore: Float32Array)
    extends BackedByFloat32Array:
  lhs =>

  override def toFloat32Array: Float32Array = Float32Array(backingStore)

  def apply(row: Int): Float =
    backingStore.get(row)

  @targetName("multiply")
  def *(rhs: Matrix4): Vec4 = Vec4(Float32Array(js.Array(
    lhs(0) * rhs(0, 0) + lhs(1) * rhs(1, 0) + lhs(2) * rhs(2, 0) + lhs(3) * rhs(
      3,
      0
    ),
    lhs(0) * rhs(0, 1) + lhs(1) * rhs(1, 1) + lhs(2) * rhs(2, 1) + lhs(3) * rhs(
      3,
      1
    ),
    lhs(0) * rhs(0, 2) + lhs(1) * rhs(1, 2) + lhs(2) * rhs(2, 2) + lhs(3) * rhs(
      3,
      2
    ),
    lhs(0) * rhs(0, 3) + lhs(1) * rhs(1, 3) + lhs(2) * rhs(2, 3) + lhs(3) * rhs(
      3,
      3
    )
  )))

  def rotate(degrees: Float, x: Float, y: Float, z: Float): Vec4 =
    lhs * Matrix4.setRotate(degrees, x, y, z)

  def scale(x: Float, y: Float, z: Float): Vec4 =
    lhs * Matrix4.setScale(x, y, z)

  def translate(x: Float, y: Float, z: Float): Vec4 =
    lhs * Matrix4.setTranslate(x, y, z)

object Vec4:
  given eqVec4(using f: Eq[BackedByFloat32Array]): Eq[Vec4] = Eq.instance:
    f.eqv

  def apply(x: Float, y: Float, z: Float, w: Float): Vec4 =
    Vec4(Float32Array(js.Array(x, y, z, w)))

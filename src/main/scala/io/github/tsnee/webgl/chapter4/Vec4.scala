package io.github.tsnee.webgl.chapter4

import cats._
import cats.syntax.all._

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

object Vec4:
  given eqVec4(using f: Eq[BackedByFloat32Array]): Eq[Vec4] = Eq.instance:
    f.eqv

  def apply(x: Float, y: Float, z: Float, w: Float): Vec4 =
    Vec4(Float32Array(js.Array(x, y, z, w)))

package io.github.tsnee.webgl.math

import cats._
import cats.syntax.all._
import narr.NArray
import slash.matrix._

import scala.annotation.targetName
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

final case class Matrix4(private val backingStore: Float32Array)
    extends BackedByFloat32Array:
  lhs =>

  /** Column major order. */
  override lazy val toFloat32Array: Float32Array = Float32Array(backingStore)

  def apply(column: Int, row: Int): Float =
    backingStore.get(column * 4 + row)

  @targetName("multiply")
  def *(rhs: Matrix4): Matrix4 = Matrix4(Float32Array(js.Array(
    lhs(0, 0) * rhs(0, 0) + lhs(1, 0) * rhs(0, 1) + lhs(2, 0) * rhs(0, 2) + lhs(3, 0) * rhs(0, 3),
    lhs(0, 1) * rhs(0, 0) + lhs(1, 1) * rhs(0, 1) + lhs(2, 1) * rhs(0, 2) + lhs(3, 1) * rhs(0, 3),
    lhs(0, 2) * rhs(0, 0) + lhs(1, 2) * rhs(0, 1) + lhs(2, 2) * rhs(0, 2) + lhs(3, 2) * rhs(0, 3),
    lhs(0, 3) * rhs(0, 0) + lhs(1, 3) * rhs(0, 1) + lhs(2, 3) * rhs(0, 2) + lhs(3, 3) * rhs(0, 3),
    lhs(0, 0) * rhs(1, 0) + lhs(1, 0) * rhs(1, 1) + lhs(2, 0) * rhs(1, 2) + lhs(3, 0) * rhs(1, 3),
    lhs(0, 1) * rhs(1, 0) + lhs(1, 1) * rhs(1, 1) + lhs(2, 1) * rhs(1, 2) + lhs(3, 1) * rhs(1, 3),
    lhs(0, 2) * rhs(1, 0) + lhs(1, 2) * rhs(1, 1) + lhs(2, 2) * rhs(1, 2) + lhs(3, 2) * rhs(1, 3),
    lhs(0, 3) * rhs(1, 0) + lhs(1, 3) * rhs(1, 1) + lhs(2, 3) * rhs(1, 2) + lhs(3, 3) * rhs(1, 3),
    lhs(0, 0) * rhs(2, 0) + lhs(1, 0) * rhs(2, 1) + lhs(2, 0) * rhs(2, 2) + lhs(3, 0) * rhs(2, 3),
    lhs(0, 1) * rhs(2, 0) + lhs(1, 1) * rhs(2, 1) + lhs(2, 1) * rhs(2, 2) + lhs(3, 1) * rhs(2, 3),
    lhs(0, 2) * rhs(2, 0) + lhs(1, 2) * rhs(2, 1) + lhs(2, 2) * rhs(2, 2) + lhs(3, 2) * rhs(2, 3),
    lhs(0, 3) * rhs(2, 0) + lhs(1, 3) * rhs(2, 1) + lhs(2, 3) * rhs(2, 2) + lhs(3, 3) * rhs(2, 3),
    lhs(0, 0) * rhs(3, 0) + lhs(1, 0) * rhs(3, 1) + lhs(2, 0) * rhs(3, 2) + lhs(3, 0) * rhs(3, 3),
    lhs(0, 1) * rhs(3, 0) + lhs(1, 1) * rhs(3, 1) + lhs(2, 1) * rhs(3, 2) + lhs(3, 1) * rhs(3, 3),
    lhs(0, 2) * rhs(3, 0) + lhs(1, 2) * rhs(3, 1) + lhs(2, 2) * rhs(3, 2) + lhs(3, 2) * rhs(3, 3),
    lhs(0, 3) * rhs(3, 0) + lhs(1, 3) * rhs(3, 1) + lhs(2, 3) * rhs(3, 2) + lhs(3, 3) * rhs(3, 3)
  )))

  def lookAt(
      eyeX: Float,
      eyeY: Float,
      eyeZ: Float,
      atX: Float,
      atY: Float,
      atZ: Float,
      upX: Float,
      upY: Float,
      upZ: Float
  ): Matrix4 =
    lhs * Matrix4.setLookAt(eyeX, eyeY, eyeZ, atX, atY, atZ, upX, upY, upZ)

  lazy val invert: Matrix4 =
    val doubles: Array[Double] = backingStore.toArray.map(_.toDouble)
    val matrix                 = Matrix[4, 4](NArray(doubles*))
    val floats                 = matrix.inverse.values.toArray.map(_.toFloat)
    Matrix4(Float32Array(js.Array(floats*)))

  def perspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix4 =
    lhs * Matrix4.setPerspective(fov, aspect, near, far)

  def rotate(degrees: Float, x: Float, y: Float, z: Float): Matrix4 =
    lhs * Matrix4.setRotate(degrees, x, y, z)

  def scale(x: Float, y: Float, z: Float): Matrix4 =
    lhs * Matrix4.setScale(x, y, z)

  def translate(x: Float, y: Float, z: Float): Matrix4 =
    lhs * Matrix4.setTranslate(x, y, z)

  lazy val transpose: Matrix4 =
    val copy = toFloat32Array
    for
      col <- 0 until 4
      row <- col until 4
    yield
      copy(col * 4 + row) = backingStore(row * 4 + col)
      copy(row * 4 + col) = backingStore(col * 4 + row)
    Matrix4(copy)

object Matrix4:
  given eqMatrix4(using f: Eq[BackedByFloat32Array]): Eq[Matrix4] = Eq.instance:
    f.eqv

  val Identity: Matrix4 =
    Matrix4(Float32Array(js.Array(
      1f, 0f, 0f, 0f,
      0f, 1f, 0f, 0f,
      0f, 0f, 1f, 0f,
      0f, 0f, 0f, 1f
    )))

  def apply(): Matrix4 = Matrix4(Float32Array(16))

  /** Params are in column-major order. */
  def apply(
      a0: Float,
      a1: Float,
      a2: Float,
      a3: Float,
      b0: Float,
      b1: Float,
      b2: Float,
      b3: Float,
      c0: Float,
      c1: Float,
      c2: Float,
      c3: Float,
      d0: Float,
      d1: Float,
      d2: Float,
      d3: Float
  ): Matrix4 = Matrix4(Float32Array(js.Array(
    a0,
    a1,
    a2,
    a3,
    b0,
    b1,
    b2,
    b3,
    c0,
    c1,
    c2,
    c3,
    d0,
    d1,
    d2,
    d3
  )))

  def setLookAt(
      eyeX: Float,
      eyeY: Float,
      eyeZ: Float,
      atX: Float,
      atY: Float,
      atZ: Float,
      upX: Float,
      upY: Float,
      upZ: Float
  ): Matrix4 =
    val forward        = Vec4.direction(atX - eyeX, atY - eyeY, atZ - eyeZ).normal
    val oldUp          = Vec4.direction(upX, upY, upZ)
    val right          = forward.cross(oldUp).normal
    val newUp          = right.cross(forward)
    val rotationMatrix = Matrix4(
      right(0),
      newUp(0),
      -forward(0),
      0f,
      right(1),
      newUp(1),
      -forward(1),
      0f,
      right(2),
      newUp(2),
      -forward(2),
      0f,
      0f,
      0f,
      0f,
      1f
    )
    rotationMatrix.translate(-eyeX, -eyeY, -eyeZ)

  def setOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 =
    Matrix4(
      2f / (right - left),
      0f,
      0f,
      0f,
      0f,
      2f / (top - bottom),
      0f,
      0f,
      0f,
      0f,
      -2f / (far - near),
      0f,
      -(right + left) / (right - left),
      -(top + bottom) / (top - bottom),
      -(far + near) / (far - near),
      1f
    )

  /** Calculate the perspective projection matrix) that defines the viewing volume specified by its arguments. */
  def setPerspective(fov: Float, aspect: Float, near: Float, far: Float): Matrix4 =
    val radians  = Math.toRadians(fov).toFloat
    val depth    = (1.0 / Math.tan(radians / 2.0)).toFloat
    val rangeInv = (1.0 / (near - far)).toFloat
    Matrix4(
      depth / aspect,
      0f,
      0f,
      0f,
      0f,
      depth,
      0f,
      0f,
      0f,
      0f,
      (far + near) * rangeInv,
      -1,
      0f,
      0f,
      2 * far * near * rangeInv,
      0f
    )

  def setRotate(degrees: Float, x: Float, y: Float, z: Float): Matrix4 =
    val w       = Math.sqrt(x * x + y * y + z * z)
    val xNorm   = x / w
    val yNorm   = y / w
    val zNorm   = z / w
    val radians = Math.PI * degrees / 180.0
    val cosB    = Math.cos(radians)
    val sinB    = Math.sin(radians)
    Matrix4(Float32Array(js.Array(
      (cosB + xNorm * xNorm * (1 - cosB)).toFloat,
      (yNorm * xNorm * (1 - cosB) + zNorm * sinB).toFloat,
      (zNorm * xNorm * (1 - cosB) - yNorm * sinB).toFloat,
      0f,
      (xNorm * yNorm * (1 - cosB) - zNorm * sinB).toFloat,
      (cosB + yNorm * yNorm * (1 - cosB)).toFloat,
      (zNorm * yNorm * (1 - cosB) + xNorm * sinB).toFloat,
      0f,
      (xNorm * zNorm * (1 - cosB) + yNorm * sinB).toFloat,
      (yNorm * zNorm * (1 - cosB) - xNorm * sinB).toFloat,
      (cosB + zNorm * zNorm * (1 - cosB)).toFloat,
      0f,
      0f,
      0f,
      0f,
      1f
    )))

  def setScale(x: Float, y: Float, z: Float): Matrix4 =
    Matrix4(Float32Array(js.Array(
      x,
      0f,
      0f,
      0f,
      0f,
      y,
      0f,
      0f,
      0f,
      0f,
      z,
      0f,
      0f,
      0f,
      0f,
      1f
    )))

  def setTranslate(x: Float, y: Float, z: Float): Matrix4 =
    Matrix4(Float32Array(js.Array(
      1f,
      0f,
      0f,
      0f,
      0f,
      1f,
      0f,
      0f,
      0f,
      0f,
      1f,
      0f,
      x,
      y,
      z,
      1f
    )))

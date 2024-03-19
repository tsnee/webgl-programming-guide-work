package io.github.tsnee.webgl.chapter4

import munit.Assertions.assertEqualsFloat
import munit.FunSuite

def assertEq(lhs: Matrix4, rhs: Matrix4): Unit =
  lhs.toFloat32Array.zip(rhs.toFloat32Array).zipWithIndex.foreach:
    case ((left, right), index) =>
      assertEqualsFloat(left, right, 1e-15, s"Problem at index $index:")

def assertEq(lhs: Vec4, rhs: Vec4): Unit =
  lhs.toFloat32Array.zip(rhs.toFloat32Array).zipWithIndex.foreach:
    case ((left, right), index) =>
      assertEqualsFloat(left, right, 1e-15, s"Problem at index $index:")

class Matrix4Suite extends FunSuite:
  test("setRotate 0 produces the identity matrix"):
    val actual   = Matrix4.setRotate(0f, 0f, 0f, 1f)
    val expected = Matrix4.Identity
    assertEq(actual, expected)

  test("setRotate 360 produces the identity matrix"):
    val actual   = Matrix4.setRotate(360f, 0f, 0f, 1f)
    val expected = Matrix4.Identity
    assertEq(actual, expected)

  test("the identity matrix squared equals itself"):
    val actual   = Matrix4.Identity * Matrix4.Identity
    val expected = Matrix4.Identity
    assertEq(actual, expected)

  test("translation followed by rotation"):
    val actual   =
      Vec4(0f, 0f, 0f, 1f).translate(0.5f, 0f, 0f).rotate(60f, 0f, 0f, 1f)
    val expected = Vec4(0.25f, (Math.sqrt(3) / 4).toFloat, 0f, 1f)
    assertEq(actual, expected)

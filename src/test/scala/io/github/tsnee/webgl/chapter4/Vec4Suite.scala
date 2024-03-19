package io.github.tsnee.webgl.chapter4

import munit.FunSuite

class Vec4Suite extends FunSuite:
  test("rotating a point about the x axis flips it"):
    val actual   = Vec4(0f, 1f, 0f, 1f) * Matrix4.setRotate(180f, 1f, 0f, 0f)
    val expected = Vec4(0f, -1f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating a point about the y axis flips it"):
    val actual   = Vec4(1f, 0f, 0f, 1f) * Matrix4.setRotate(180f, 0f, 1f, 0f)
    val expected = Vec4(-1f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating a point about the z axis flips it"):
    val actual   = Vec4(1f, 1f, 0f, 1f) * Matrix4.setRotate(180f, 0f, 0f, 1f)
    val expected = Vec4(-1f, -1f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating a point 90 degrees about the z axis flips the x coordinate"):
    val actual   = Vec4(1f, 1f, 0f, 1f) * Matrix4.setRotate(90f, 0f, 0f, 1f)
    val expected = Vec4(-1f, 1f, 0f, 1f)
    assertEq(actual, expected)

  test("scaling a point by 2 doubles the x coordinate"):
    val actual   = Vec4(1f, 1f, 0f, 1f).scale(2f, 1f, 1f)
    val expected = Vec4(2f, 1f, 0f, 1f)
    assertEq(actual, expected)

  test("translating the origin right increases the x coordinate"):
    val actual   = Vec4(0f, 0f, 0f, 1f).translate(1f, 0f, 0f)
    val expected = Vec4(1f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("matrix multiplication yields the correct result"):
    val actual   = Vec4(1f, 2f, 3f, 4f) *
      Matrix4(2f, 1f, 0f, 0f, 1f, 0f, -1f, 0f, 0f, -1f, 2f, 0f, 0f, 0f, 0f, 1f)
    val expected = Vec4(4f, -2f, 4f, 4f)
    assertEq(actual, expected)

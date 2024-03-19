package io.github.tsnee.webgl.chapter4

import munit.FunSuite

class Vec4Suite extends FunSuite:
  test("rotating a point about the x axis flips it"):
    val actual   = Matrix4.setRotate(180f, 1f, 0f, 0f) * Vec4(0f, 1f, 0f, 1f)
    val expected = Vec4(0f, -1f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating a point about the y axis flips it"):
    val actual   = Matrix4.setRotate(180f, 0f, 1f, 0f) * Vec4(1f, 0f, 0f, 1f)
    val expected = Vec4(-1f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating a point about the z axis flips it"):
    val actual   = Matrix4.setRotate(180f, 0f, 0f, 1f) * Vec4(1f, 1f, 0f, 1f)
    val expected = Vec4(-1f, -1f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating a point 90 degrees about the z axis flips the x coordinate"):
    val actual   = Matrix4.setRotate(90f, 0f, 0f, 1f) * Vec4(1f, 1f, 0f, 1f)
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
    val actual   =
      Matrix4(2f, 1f, 0f, 0f, 1f, 0f, -1f, 0f, 0f, -1f, 2f, 0f, 0f, 0f, 0f, 1f) * Vec4(1f, 2f, 3f, 4f)
    val expected = Vec4(4f, -2f, 4f, 4f)
    assertEq(actual, expected)

  test("translation followed by rotation"):
    val actual   = Vec4(0f, 0f, 0f, 1f).translate(0.5f, 0f, 0f).rotate(60f, 0f, 0f, 1f)
    val expected = Vec4(0.25f, (Math.sqrt(3) / 4).toFloat, 0f, 1f)
    assertEq(actual, expected)

  test("rotation followed by translation is the same as translation by itself"):
    val actual   = Vec4(0f, 0f, 0f, 1f).rotate(60f, 0f, 0f, 1f).translate(0.5f, 0f, 0f)
    val expected = Vec4(0.5f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating the origin about the x axis does nothing"):
    val actual   = Vec4(0f, 0f, 0f, 1f).rotate(180f, 1f, 0f, 0f)
    val expected = Vec4(0f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating the origin about the y axis does nothing"):
    val actual   = Vec4(0f, 0f, 0f, 1f).rotate(180f, 0f, 1f, 0f)
    val expected = Vec4(0f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating the origin about the z axis does nothing"):
    val actual   = Vec4(0f, 0f, 0f, 1f).rotate(180f, 0f, 0f, 1f)
    val expected = Vec4(0f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating 1, 0, 0 about the x axis does nothing"):
    val actual   = Vec4(1f, 0f, 0f, 1f).rotate(90f, 1f, 0f, 0f)
    val expected = Vec4(1f, 0f, 0f, 1f)
    assertEq(actual, expected)

  test("rotating 1, 0, 0 about the y axis changes x to z"):
    val actual   = Vec4(1f, 0f, 0f, 1f).rotate(90f, 0f, 1f, 0f)
    val expected = Vec4(0f, 0f, -1f, 1f)
    assertEq(actual, expected)

  test("rotating 1, 0, 0 about the z axis changes x to y"):
    val actual   = Vec4(1f, 0f, 0f, 1f).rotate(90f, 0f, 0f, 1f)
    val expected = Vec4(0f, 1f, 0f, 1f)
    assertEq(actual, expected)

package io.github.tsnee.webgl.math

import scala.annotation.targetName
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

extension (lhs: Matrix4)
  @targetName("multiply")
  def *(rhs: Vec4): Vec4 = Vec4(Float32Array(js.Array(
    lhs(0, 0) * rhs(0) + lhs(1, 0) * rhs(1) + lhs(2, 0) * rhs(2) + lhs(3, 0) * rhs(3),
    lhs(0, 1) * rhs(0) + lhs(1, 1) * rhs(1) + lhs(2, 1) * rhs(2) + lhs(3, 1) * rhs(3),
    lhs(0, 2) * rhs(0) + lhs(1, 2) * rhs(1) + lhs(2, 2) * rhs(2) + lhs(3, 2) * rhs(3),
    lhs(0, 3) * rhs(0) + lhs(1, 3) * rhs(1) + lhs(2, 3) * rhs(2) + lhs(3, 3) * rhs(3)
  )))

package io.github.tsnee.webgl.math

import cats._

import scala.scalajs.js.typedarray.Float32Array

trait BackedByFloat32Array:
  def toFloat32Array: Float32Array

object BackedByFloat32Array:
  given eqBackedByFloat32Array(using f: Eq[Float]): Eq[BackedByFloat32Array]
  with
    def eqv(lhs: BackedByFloat32Array, rhs: BackedByFloat32Array): Boolean =
      lhs.toFloat32Array.zip(rhs.toFloat32Array).forall(f.eqv)

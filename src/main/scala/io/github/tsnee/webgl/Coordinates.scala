package io.github.tsnee.webgl

import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._

sealed trait CoordinateSpace[T : Numeric]
object CoordinateSpace:
  type CanvasSpace = CoordinateSpace[Int :| Not[Negative]]
  type WebGLSpace  = CoordinateSpace[Float :| GreaterEqual[-1.0] & LessEqual[1.0]]

case class Coordinates[S <: CoordinateSpace[T], T : Numeric](x: T, y: T)

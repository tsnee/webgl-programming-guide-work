package io.github.tsnee.webgl

import cats._
import cats.syntax.all._
import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.cats.given
import io.github.tsnee.webgl.types._

final case class ExerciseDescriptor(
    label: String,
    chapter: Chapter,
    position: Int,
    height: Height,
    width: Width,
    panel: (Height, Width) => Element
)

object ExerciseDescriptor:
  given Ordering[ExerciseDescriptor] with
    override def compare(x: ExerciseDescriptor, y: ExerciseDescriptor): Int =
      if x.chapter =!= y.chapter
      then x.chapter.compare(y.chapter)
      else x.position.compare(y.position)
  given eqInstance(using ordering: Ordering[ExerciseDescriptor]): Eq[ExerciseDescriptor] with
    def eqv(x: ExerciseDescriptor, y: ExerciseDescriptor): Boolean =
      ordering.compare(x, y) === 0

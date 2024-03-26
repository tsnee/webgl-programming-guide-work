package io.github.tsnee.webgl

import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import org.scalajs.dom.HTMLCanvasElement

object types:
  type Canvas = ReactiveHtmlElement[HTMLCanvasElement]

  type Chapter = Int :| Positive
  object Chapter extends RefinedTypeOps.Transparent[Chapter]

  type FragmentShaderSource = String :| Not[Blank]
  object FragmentShaderSource extends RefinedTypeOps.Transparent[FragmentShaderSource]

  type Height = Int :| Positive
  object Height extends RefinedTypeOps.Transparent[Height]

  type VertexShaderSource = String :| Not[Blank]
  object VertexShaderSource extends RefinedTypeOps.Transparent[VertexShaderSource]

  type Width = Int :| Positive
  object Width extends RefinedTypeOps.Transparent[Width]

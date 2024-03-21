package io.github.tsnee.webgl

import org.scalajs.dom._

trait Exercise:
  def label: String

  def width: Int = Exercise.canvasWidth + 25

  def height: Int = Exercise.canvasHeight + 25

  def build: Element =
    val component = document.createElement("div")
    component.setAttribute("id", Exercise.componentId)
    document.createElement("canvas") match
      case canvas: HTMLCanvasElement =>
        canvas.height = Exercise.canvasHeight
        canvas.width = Exercise.canvasWidth
        initialize(canvas)
        component.appendChild(canvas)
        component

  def initialize(canvas: HTMLCanvasElement): Unit

object Exercise:
  val canvasWidth: Int    = 400
  val canvasHeight: Int   = 400
  val componentId: String = "exercise"

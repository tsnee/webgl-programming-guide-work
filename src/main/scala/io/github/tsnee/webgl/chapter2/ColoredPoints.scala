package io.github.tsnee.webgl.chapter2

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl.common.ExerciseBuilder
import io.github.tsnee.webgl.types._
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLUniformLocation

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ColoredPoints:
  val vertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: FragmentShaderSource = """
precision mediump float;
uniform vec4 u_FragColor;
void main() {
  gl_FragColor = u_FragColor;
}
"""

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(
      canvasWidth: Width,
      canvasHeight: Height
    )

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val aPosition                                    = gl.getAttribLocation(program, "a_Position")
    val uFragColor                                   = gl.getUniformLocation(program, "u_FragColor")
    val (clickedPointStream, clickedPointListSignal) = normalizeAndRecordClicks(canvas.ref)
    canvas.amend(onClick --> clickedPointStream, clickedPointListSignal --> draw(gl, aPosition, uFragColor))

  private type PointTuple = (Float, Float, Float)
  private type ColorTuple = (Float, Float, Float, Float)

  private def draw(
      gl: WebGLRenderingContext,
      aPosition: Int,
      uFragColor: WebGLUniformLocation
  )(
      pointsAndColors: List[(PointTuple, ColorTuple)]
  ): Unit =
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    pointsAndColors.foreach: pointAndColor =>
      val ((x, y, z), (r, g, b, a)) = pointAndColor
      gl.vertexAttrib3fv(aPosition, Float32Array(js.Array(x, y, z)))
      gl.uniform4fv(uFragColor, Float32Array(js.Array(r, g, b, a)))
      gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  private def normalizeAndRecordClicks(
      canvasElement: HTMLCanvasElement
  ): (Observer[MouseEvent], Signal[List[(PointTuple, ColorTuple)]]) =
    val pointEventBus                  = EventBus[(PointTuple, ColorTuple)]()
    val clickedPointAndColorStream     = pointEventBus.writer.contramap[MouseEvent]: evt =>
      val rect              = canvasElement.getBoundingClientRect()
      val x                 = ((evt.clientX - rect.left) - canvasElement.width / 2) / (canvasElement.width / 2)
      val y                 = (canvasElement.height / 2 - (evt.clientY - rect.top)) / (canvasElement.height / 2)
      val point: PointTuple = (x.toFloat, y.toFloat, 0)
      val color: ColorTuple =
        if x >= 0.0 && y >= 0.0 then (1, 0, 0, 1)
        else if x < 0.0 && y < 0.0 then (0, 1, 0, 1)
        else (1, 1, 1, 1)
      (point, color)
    val clickedPointAndColorListSignal =
      pointEventBus.events.scanLeft(Nil)((list, pointAndColor) => pointAndColor :: list)
    (clickedPointAndColorStream, clickedPointAndColorListSignal)

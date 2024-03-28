package io.github.tsnee.webgl.chapter2

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ExerciseBuilder
import io.github.tsnee.webgl.types._
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ClickedPoints:
  val vertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: FragmentShaderSource = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def panel(height: Height, width: Width): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val aPosition                                    = gl.getAttribLocation(program, "a_Position")
    val (clickedPointStream, clickedPointListSignal) = normalizeAndRecordClicks(canvas.ref)
    canvas.amend(onClick --> clickedPointStream, clickedPointListSignal --> draw(gl, aPosition))

  private def normalizeAndRecordClicks(
      canvasElement: HTMLCanvasElement
  ): (Observer[MouseEvent], Signal[List[(Float, Float, Float)]]) =
    val pointEventBus          = EventBus[(Float, Float, Float)]()
    val clickedPointStream     = pointEventBus.writer.contramap[MouseEvent]: evt =>
      val rect = canvasElement.getBoundingClientRect()
      val x    = ((evt.clientX - rect.left) - canvasElement.width / 2) / (canvasElement.width / 2)
      val y    = (canvasElement.height / 2 - (evt.clientY - rect.top)) / (canvasElement.height / 2)
      (x.toFloat, y.toFloat, 0f)
    val clickedPointListSignal =
      pointEventBus.events.scanLeft(Nil)((list, point) => point :: list)
    (clickedPointStream, clickedPointListSignal)

  private def draw(gl: WebGLRenderingContext, aPosition: Int)(pointList: List[(Float, Float, Float)]): Unit =
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    pointList.foreach:
      case (x, y, z) =>
        gl.vertexAttrib3fv(aPosition, Float32Array(js.Array(x, y, z)))
        gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

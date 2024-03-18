package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ClickedPoints:
  val vertexShaderSource: String = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: String = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def initialize(canvas: Canvas): Unit =
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run(canvas, _, _)
    )

  private def click(
      ev: MouseEvent,
      gl: WebGLRenderingContext,
      canvas: Canvas,
      aPosition: Int,
      points: js.Array[Float]
  ): Unit =
    val rect = canvas.getBoundingClientRect()
    val x    = ((ev.clientX - rect.left) - canvas.width / 2) / (canvas.width / 2)
    val y    = (canvas.height / 2 - (ev.clientY - rect.top)) / (canvas.height / 2)
    points.addAll(List(x.toFloat, y.toFloat, 0f))
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    points.sliding(3, 3).foreach: window =>
      gl.vertexAttrib3fv(aPosition, Float32Array(window))
      gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  private def run(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val aPosition    = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val storedPoints = js.Array[Float]()
    canvas.addEventListener(
      "click",
      click(_, gl, canvas, aPosition, storedPoints)
    )

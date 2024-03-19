package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ColoredPoints:
  val vertexShaderSource: String = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: String = """
precision mediump float;
uniform vec4 u_FragColor;
void main() {
  gl_FragColor = u_FragColor;
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
      canvas: Canvas,
      gl: WebGLRenderingContext,
      aPosition: Int,
      uFragColor: WebGLUniformLocation,
      points: js.Array[Float],
      colors: js.Array[Float]
  ): Unit =
    val rect  = canvas.getBoundingClientRect()
    val x     = ((ev.clientX - rect.left) - canvas.width / 2) / (canvas.width / 2)
    val y     = (canvas.height / 2 - (ev.clientY - rect.top)) / (canvas.height / 2)
    points.addAll(js.Array(x.toFloat, y.toFloat, 0f))
    val color = if x >= 0.0 && y >= 0.0 then js.Array(1f, 0f, 0f)
    else if x < 0.0 && y < 0.0 then js.Array(0f, 1f, 0f)
    else js.Array(1f, 1f, 1f)
    colors.addAll(color)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    points.zip(colors).sliding(3, 3).foreach: window =>
      val (point, color) = window.unzip
      gl.uniform4fv(uFragColor, Float32Array(color :+ 1f))
      gl.vertexAttrib3fv(aPosition, Float32Array(point))
      gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  private def run(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val aPosition  = gl.getAttribLocation(program, "a_Position")
    val uFragColor = gl.getUniformLocation(program, "u_FragColor")
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val points     = js.Array[Float]()
    val colors     = js.Array[Float]()
    canvas.addEventListener(
      "click",
      click(_, canvas, gl, aPosition, uFragColor, points, colors)
    )

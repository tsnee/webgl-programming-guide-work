package io.github.tsnee.webgl.chapter3

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object MultiPoint:
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
      run
    )

  private def run(
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val vertices     = Float32Array(js.Array(0f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f))
    val vertexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(
      WebGLRenderingContext.ARRAY_BUFFER,
      vertices,
      WebGLRenderingContext.STATIC_DRAW
    )
    val aPosition    = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttribPointer(
      indx = aPosition,
      size = 2,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(aPosition)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(
      mode = WebGLRenderingContext.POINTS,
      first = 0,
      count = vertices.size / 2
    )

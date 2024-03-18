package io.github.tsnee.webgl.chapter3

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object RotatedTriangleMatrix:
  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
uniform mat4 u_xformMatrix;
void main() {
  gl_Position = u_xformMatrix * a_Position;
}
"""

  val fragmentShaderSource: String =
    """
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
    val uXformMatrix = gl.getUniformLocation(program, "u_xformMatrix")
    val degrees      = 90f
    val radians      = Math.PI * degrees / 180f
    val cosB         = Math.cos(radians).toFloat
    val sinB         = Math.sin(radians).toFloat
    val xformMatrix  = Float32Array(js.Array(
      cosB,
      sinB,
      0f,
      0f,
      -sinB,
      cosB,
      0f,
      0f,
      0f,
      0f,
      1f,
      0f,
      0f,
      0f,
      0f,
      1f
    ))
    gl.uniformMatrix4fv(
      location = uXformMatrix,
      transpose = false,
      value = xformMatrix
    )
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = vertices.size / 2
    )

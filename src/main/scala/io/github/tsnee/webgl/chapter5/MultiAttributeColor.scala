package io.github.tsnee.webgl.chapter5

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object MultiAttributeColor:
  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
varying vec4 v_Color;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
  v_Color = a_Color;
}
"""

  val fragmentShaderSource: String =
    """
precision highp float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
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
    val verticesColors    = Float32Array(js.Array(0f, 0.5f, 1f, 0f, 0f, -0.5f, -0.5f, 0f, 1f, 0f, 0.5f, -0.5f, 0f, 0f, 1f))
    val vertexColorBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexColorBuffer)
    gl.bufferData(
      WebGLRenderingContext.ARRAY_BUFFER,
      verticesColors,
      WebGLRenderingContext.STATIC_DRAW
    )
    val floatSizeBytes    = Float32Array.BYTES_PER_ELEMENT
    val aPosition         = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttribPointer(
      indx = aPosition,
      size = 2,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = floatSizeBytes * 5,
      offset = 0
    )
    gl.enableVertexAttribArray(aPosition)
    val aColor            = gl.getAttribLocation(program, "a_Color")
    gl.vertexAttribPointer(
      indx = aColor,
      size = 3,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = floatSizeBytes * 5,
      offset = floatSizeBytes * 2
    )
    gl.enableVertexAttribArray(aColor)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(
      mode = WebGLRenderingContext.POINTS,
      first = 0,
      count = verticesColors.size / 5
    )

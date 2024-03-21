package io.github.tsnee.webgl.chapter5

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object MultiAttributeSizeInterleaved extends Exercise:
  override val label: String = "MultiAttributeSize_Interleaved"

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute float a_PointSize;
void main() {
  gl_Position = a_Position;
  gl_PointSize = a_PointSize;
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
    val verticesSizes    = Float32Array(js.Array(0f, 0.5f, 10f, -0.5f, -0.5f, 20f, 0.5f, -0.5f, 30f))
    val vertexSizeBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexSizeBuffer)
    gl.bufferData(
      WebGLRenderingContext.ARRAY_BUFFER,
      verticesSizes,
      WebGLRenderingContext.STATIC_DRAW
    )
    val floatSizeBytes   = Float32Array.BYTES_PER_ELEMENT
    val aPosition        = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttribPointer(
      indx = aPosition,
      size = 2,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = floatSizeBytes * 3,
      offset = 0
    )
    gl.enableVertexAttribArray(aPosition)
    val aPointSize       = gl.getAttribLocation(program, "a_PointSize")
    gl.vertexAttribPointer(
      indx = aPointSize,
      size = 1,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = floatSizeBytes * 3,
      offset = floatSizeBytes * 2
    )
    gl.enableVertexAttribArray(aPointSize)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(
      mode = WebGLRenderingContext.POINTS,
      first = 0,
      count = verticesSizes.size / 3
    )

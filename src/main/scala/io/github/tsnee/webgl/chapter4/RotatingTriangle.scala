package io.github.tsnee.webgl.chapter4

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object RotatingTriangle extends Exercise:
  override def label: String = "RotatingTriangle"

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
uniform mat4 u_modelMatrix;
void main() {
  gl_Position = u_modelMatrix * a_Position;
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
    gl.useProgram(program)
    val uModelMatrix = gl.getUniformLocation(program, "u_modelMatrix")
    val now          = js.Date.now()
    tick(gl, vertices.size / 2, 0f, uModelMatrix, now)(now)

  private def tick(
      gl: WebGLRenderingContext,
      numVertices: Int,
      currentAngle: Float,
      uModelMatrix: WebGLUniformLocation,
      lastCallTs: Double
  )(
      currentTs: Double
  ): Unit =
    val elapsedSeconds   = (currentTs - lastCallTs) / 1000
    val degreesPerSecond = 45.0
    val nextAngle        = (currentAngle + degreesPerSecond * elapsedSeconds).toFloat % 360f
    draw(gl, numVertices, nextAngle, uModelMatrix)
    val _                = window.requestAnimationFrame(
      tick(gl, numVertices, nextAngle, uModelMatrix, currentTs)(_)
    )

  private def draw(
      gl: WebGLRenderingContext,
      numVertices: Int,
      currentAngle: Float,
      uModelMatrix: WebGLUniformLocation
  ): Unit =
    gl.uniformMatrix4fv(
      location = uModelMatrix,
      transpose = false,
      value = Matrix4.setRotate(currentAngle, 0f, 0f, 1f).toFloat32Array
    )
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )

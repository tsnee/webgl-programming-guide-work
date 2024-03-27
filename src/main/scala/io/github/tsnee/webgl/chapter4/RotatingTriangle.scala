package io.github.tsnee.webgl.chapter4

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLUniformLocation
import org.scalajs.dom.window

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object RotatingTriangle:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
uniform mat4 u_modelMatrix;
void main() {
  gl_Position = u_modelMatrix * a_Position;
}
"""

  val fragmentShaderSource: FragmentShaderSource =
    """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val vertices     = Float32Array(js.Array[Float](0, 0.5, -0.5, -0.5, 0.5, -0.5))
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Position",
      size = 2,
      stride = 0,
      offset = 0
    )
    gl.clearColor(0, 0, 0, 1)
    gl.useProgram(program)
    val uModelMatrix = gl.getUniformLocation(program, "u_modelMatrix")
    val now          = js.Date.now()
    tick(gl, vertices.size / 2, 0, uModelMatrix, now)(now)

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
      value = Matrix4.setRotate(currentAngle, 0, 0, 1).toFloat32Array
    )
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )

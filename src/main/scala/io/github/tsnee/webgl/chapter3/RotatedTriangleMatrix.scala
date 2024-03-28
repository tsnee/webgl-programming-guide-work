package io.github.tsnee.webgl.chapter3

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ExerciseBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.types._
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object RotatedTriangleMatrix:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
uniform mat4 u_xformMatrix;
void main() {
  gl_Position = u_xformMatrix * a_Position;
}
"""

  val fragmentShaderSource: FragmentShaderSource =
    """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def panel(height: Height, width: Width): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

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
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uXformMatrix = gl.getUniformLocation(program, "u_xformMatrix")
    val degrees      = 90
    val radians      = Math.PI * degrees / 180
    val cosB         = Math.cos(radians).toFloat
    val sinB         = Math.sin(radians).toFloat
    val xformMatrix  = Float32Array(js.Array[Float](
      cosB,
      sinB,
      0,
      0,
      -sinB,
      cosB,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1
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

package io.github.tsnee.webgl.chapter3

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.types._
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object RotatedTriangle:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
uniform float u_CosB, u_SinB;
void main() {
  gl_Position.x = a_Position.x * u_CosB - a_Position.y * u_SinB;
  gl_Position.y = a_Position.x * u_SinB + a_Position.y * u_CosB;
  gl_Position.z = a_Position.z;
  gl_Position.w = 1.0;
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
    val vertices = Float32Array(js.Array[Float](0, 0.5, -0.5, -0.5, 0.5, -0.5))
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Position", size = 2, stride = 0, offset = 0)
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uCosB    = gl.getUniformLocation(program, "u_CosB")
    val uSinB    = gl.getUniformLocation(program, "u_SinB")
    val degrees  = 90f
    val radians  = Math.PI * degrees / 180f
    val cosB     = Math.cos(radians)
    val sinB     = Math.sin(radians)
    gl.uniform1f(uCosB, cosB)
    gl.uniform1f(uSinB, sinB)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = vertices.size / 2
    )

package io.github.tsnee.webgl.chapter5

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

object HelloTriangleFragCoord:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
}
"""

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
uniform float u_Width;
uniform float u_Height;
void main() {
  gl_FragColor = vec4(gl_FragCoord.x/u_Width, 0.0, gl_FragCoord.y/u_Height, 1.0);
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
    val uWidth   = gl.getUniformLocation(program, "u_Width")
    gl.uniform1f(uWidth, gl.drawingBufferWidth)
    val uHeight  = gl.getUniformLocation(program, "u_Height")
    gl.uniform1f(uHeight, gl.drawingBufferHeight)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = vertices.size / 2
    )

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

abstract class AbstractHelloTriangle(arrayDrawingMode: Int):
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
}
"""

  val fragmentShaderSource: FragmentShaderSource =
    """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(
      canvasWidth: Width,
      canvasHeight: Height
    )

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val vertices = Float32Array(js.Array(0f, 0.5f, -0.5f, -0.5f, 0.5f, -0.5f))
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
    gl.drawArrays(
      mode = arrayDrawingMode,
      first = 0,
      count = vertices.size / 2
    )

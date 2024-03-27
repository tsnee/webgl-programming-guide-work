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

object MultiAttributeSizeInterleaved:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute float a_PointSize;
void main() {
  gl_Position = a_Position;
  gl_PointSize = a_PointSize;
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
    val verticesSizes  = Float32Array(js.Array[Float](0, 0.5, 10, -0.5, -0.5, 20, 0.5, -0.5, 30))
    VertexBufferObject.initializeVbo(gl, verticesSizes)
    val floatSizeBytes = Float32Array.BYTES_PER_ELEMENT
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Position",
      size = 2,
      stride = floatSizeBytes * 3,
      offset = 0
    )
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_PointSize",
      size = 1,
      stride = floatSizeBytes * 3,
      offset = floatSizeBytes * 2
    )
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    gl.drawArrays(
      mode = WebGLRenderingContext.POINTS,
      first = 0,
      count = verticesSizes.size / 3
    )

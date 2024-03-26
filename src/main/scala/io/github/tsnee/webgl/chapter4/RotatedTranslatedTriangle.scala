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

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object RotatedTranslatedTriangle:
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
    val vertices     = Float32Array(js.Array(0f, 0.3f, -0.3f, -0.3f, 0.3f, -0.3f))
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Position", size = 2, stride = 0, offset = 0)
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uModelMatrix = gl.getUniformLocation(program, "u_modelMatrix")
    val modelMatrix  = Matrix4.setRotate(60f, 0f, 0f, 1f).translate(0.5f, 0f, 0f)
    gl.uniformMatrix4fv(
      location = uModelMatrix,
      transpose = false,
      value = modelMatrix.toFloat32Array
    )
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = vertices.size / 2
    )

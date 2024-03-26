package io.github.tsnee.webgl.chapter7

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

object LookAtRotatedTrianglesMvMatrix:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_ModelViewMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_ModelViewMatrix * a_Position;
  v_Color = a_Color;
}
"""

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize        = Float32Array.BYTES_PER_ELEMENT
    val verticesColors   = Float32Array(js.Array[Float](
      0.0, 0.5, -0.4, 0.4, 1.0, 0.4, // The back green triangle
      -0.5, -0.5, -0.4, 0.4, 1.0, 0.4,
      0.5, -0.5, -0.4, 1.0, 0.4, 0.4,
      0.5, 0.4, -0.2, 1.0, 0.4, 0.4, // The middle yellow triangle
      -0.5, 0.4, -0.2, 1.0, 1.0, 0.4,
      0.0, -0.6, -0.2, 1.0, 1.0, 0.4,
      0.0, 0.5, 0.0, 0.4, 0.4, 1.0,  // The front blue triangle
      -0.5, -0.5, 0.0, 0.4, 0.4, 1.0,
      0.5, -0.5, 0.0, 1.0, 0.4, 0.4
    ))
    VertexBufferObject.initializeVbo(gl, verticesColors)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uModelViewMatrix = gl.getUniformLocation(program, "u_ModelViewMatrix")
    val viewMatrix       = Matrix4.setLookAt(0.20f, 0.25f, 0.25f, 0f, 0f, 0f, 0f, 1f, 0f)
    val modelMatrix      = Matrix4.setRotate(-10, 0, 0, 1)
    val modelViewMatrix  = viewMatrix * modelMatrix
    gl.uniformMatrix4fv(
      location = uModelViewMatrix,
      transpose = false,
      value = modelViewMatrix.toFloat32Array
    )
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = verticesColors.size / 6
    )

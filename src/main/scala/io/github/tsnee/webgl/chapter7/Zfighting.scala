package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object Zfighting:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_ViewProjMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_ViewProjMatrix * a_Position;
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
    val floatSize       = Float32Array.BYTES_PER_ELEMENT
    val verticesColors  = Float32Array(js.Array[Float](
      0, 2.5, -5, 0, 1, 0, // The green triangle
      -2.5, -2.5, -5, 0, 1, 0,
      2.5, -2.5, -5, 1, 0, 0,
      0, 3, -5, 1, 0, 0,   // The yellow triangle
      -3, -3, -5, 1, 1, 0,
      3, -3, -5, 1, 1, 0
    ))
    VertexBufferObject.initializeVbo(gl, verticesColors)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.enable(WebGLRenderingContext.POLYGON_OFFSET_FILL)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.useProgram(program)
    val uViewProjMatrix = gl.getUniformLocation(program, "u_ViewProjMatrix")
    val viewProjMatrix  = Matrix4
      .setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
      .lookAt(3.06, 2.5, 10.0, 0, 0, -2, 0, 1, 0)
    gl.uniformMatrix4fv(uViewProjMatrix, false, viewProjMatrix.toFloat32Array)
    val numVertices     = verticesColors.size / 6
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices / 2
    )
    gl.polygonOffset(1f, 1f)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = numVertices / 2,
      count = numVertices / 2
    )

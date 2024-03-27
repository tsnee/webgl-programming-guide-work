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

object DepthBuffer:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_MvpMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
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
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    val verticesColors = Float32Array(js.Array(
      0f, 1.0f, 0.0f, 0.4f, 0.4f, 1.0f,  // The front blue one
      -0.5f, -1.0f, 0.0f, 0.4f, 0.4f, 1.0f,
      0.5f, -1.0f, 0.0f, 1.0f, 0.4f, 0.4f,
      0f, 1.0f, -2.0f, 1.0f, 1.0f, 0.4f, // The middle yellow one
      -0.5f, -1.0f, -2.0f, 1.0f, 1.0f, 0.4f,
      0.5f, -1.0f, -2.0f, 1.0f, 0.4f, 0.4f,
      0f, 1.0f, -4.0f, 0.4f, 1.0f, 0.4f, // The back green one
      -0.5f, -1.0f, -4.0f, 0.4f, 1.0f, 0.4f,
      0.5f, -1.0f, -4.0f, 1.0f, 0.4f, 0.4f
    ))
    VertexBufferObject.initializeVbo(gl, verticesColors)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 3, floatSize * 6, 0)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.useProgram(program)
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val viewMatrix     = Matrix4.setLookAt(0f, 0f, 5f, 0f, 0f, -100f, 0f, 1f, 0f)
    val projMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val precalculated  = projMatrix * viewMatrix
    val numVertices    = verticesColors.size / 6
    draw(gl, numVertices, uMvpMatrix, precalculated)

  private def draw(
      gl: WebGLRenderingContext,
      numVertices: Int,
      uMvpMatrix: WebGLUniformLocation,
      precalculated: Matrix4
  ): Unit =
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.uniformMatrix4fv(
      location = uMvpMatrix,
      transpose = false,
      value = (precalculated * Matrix4.setTranslate(0.75f, 0f, 0f)).toFloat32Array
    )
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )
    gl.uniformMatrix4fv(
      location = uMvpMatrix,
      transpose = false,
      value = (precalculated * Matrix4.setTranslate(-0.75f, 0f, 0f)).toFloat32Array
    )
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )

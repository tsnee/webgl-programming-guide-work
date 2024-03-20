package io.github.tsnee.webgl.chapter7

import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object LookAtTriangles:
  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_ViewMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_ViewMatrix * a_Position;
  v_Color = a_Color;
}
"""

  val fragmentShaderSource: String =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  def initialize(canvas: Canvas): Unit =
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run
    )

  private def run(
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    val verticesColors = Float32Array(js.Array(
      0.0f, 0.5f, -0.4f, 0.4f, 1.0f, 0.4f, // The back green triangle
      -0.5f, -0.5f, -0.4f, 0.4f, 1.0f, 0.4f,
      0.5f, -0.5f, -0.4f, 1.0f, 0.4f, 0.4f,
      0.5f, 0.4f, -0.2f, 1.0f, 0.4f, 0.4f, // The middle yellow triangle
      -0.5f, 0.4f, -0.2f, 1.0f, 1.0f, 0.4f,
      0.0f, -0.6f, -0.2f, 1.0f, 1.0f, 0.4f,
      0.0f, 0.5f, 0.0f, 0.4f, 0.4f, 1.0f,  // The front blue triangle
      -0.5f, -0.5f, 0.0f, 0.4f, 0.4f, 1.0f,
      0.5f, -0.5f, 0.0f, 1.0f, 0.4f, 0.4f
    ))
    initializeVbo(gl, verticesColors)
    enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uViewMatrix    = gl.getUniformLocation(program, "u_ViewMatrix")
    val viewMatrix     = Matrix4.setLookAt(0.20f, 0.25f, 0.25f, 0f, 0f, 0f, 0f, 1f, 0f)
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = viewMatrix.toFloat32Array
    )
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = verticesColors.size / 6
    )

  private def initializeVbo(gl: WebGLRenderingContext, array: Float32Array): Unit =
    val vertexTexCoordsBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexTexCoordsBuffer)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, array, WebGLRenderingContext.STATIC_DRAW)

  private def enableFloatAttribute(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      attributeName: String,
      size: Int,
      stride: Int,
      offset: Int
  ): Unit =
    val attribute = gl.getAttribLocation(program, attributeName)
    gl.vertexAttribPointer(
      indx = attribute,
      size = size,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = stride,
      offset = offset
    )
    gl.enableVertexAttribArray(attribute)

package io.github.tsnee.webgl.chapter7

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object Zfighting extends Exercise:
  override val label: String = "Zfighting"

  val vertexShaderSource: String =
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
    val floatSize       = Float32Array.BYTES_PER_ELEMENT
    val verticesColors  = Float32Array(js.Array(
      0f, 2.5f, -5f, 0f, 1f, 0f, // The green triangle
      -2.5f, -2.5f, -5f, 0f, 1f, 0f,
      2.5f, -2.5f, -5f, 1f, 0f, 0f,
      0f, 3f, -5f, 1f, 0f, 0f,   // The yellow triangle
      -3f, -3f, -5f, 1f, 1f, 0f,
      3f, -3f, -5f, 1f, 1f, 0f
    ))
    initializeVbo(gl, verticesColors)
    enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
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

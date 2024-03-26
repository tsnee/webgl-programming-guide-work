package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L.{Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object DepthBuffer extends Exercise:
  override val label: String = "DepthBuffer"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  val vertexShaderSource: String =
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
    initializeVbo(gl, verticesColors)
    enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
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

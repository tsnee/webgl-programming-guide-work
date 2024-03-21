package io.github.tsnee.webgl.chapter7

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object LookAtTrianglesWithKeys extends Exercise:
  override val label: String = "LookAtTrianglesWithKeys"

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

  private val gViewMatrixValues                                = Array(0.20f, 0.25f, 0.25f, 0f, 0f, 0f, 0f, 1f, 0f)
  // indices into gViewMatrixValues
  private val (eyeX, eyeY, eyeZ, atX, atY, atZ, upX, upY, upZ) = (0, 1, 2, 3, 4, 5, 6, 7, 8)

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
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setLookAt(
        gViewMatrixValues(eyeX),
        gViewMatrixValues(eyeY),
        gViewMatrixValues(eyeZ),
        gViewMatrixValues(atX),
        gViewMatrixValues(atY),
        gViewMatrixValues(atZ),
        gViewMatrixValues(upX),
        gViewMatrixValues(upY),
        gViewMatrixValues(upZ)
      ).toFloat32Array
    )
    val numVertices    = verticesColors.size / 6
    document.addEventListener("keydown", keyDown(gl, numVertices, uViewMatrix)(_))
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )

  private def keyDown(
      gl: WebGLRenderingContext,
      numVertices: Int,
      uViewMatrix: WebGLUniformLocation
  )(evt: KeyboardEvent): Unit =
    evt.keyCode match
      case 37 | 72 => gViewMatrixValues(eyeX) -= 0.01f // Left arrow or 'h'
      case 39 | 76 => gViewMatrixValues(eyeX) += 0.01f // Right arrow or 'l'
      case 40 | 74 => gViewMatrixValues(eyeY) -= 0.01f // Down arrow or 'j'
      case 38 | 75 => gViewMatrixValues(eyeY) += 0.01f // Up arrow or 'k'
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setLookAt(
        gViewMatrixValues(eyeX),
        gViewMatrixValues(eyeY),
        gViewMatrixValues(eyeZ),
        gViewMatrixValues(atX),
        gViewMatrixValues(atY),
        gViewMatrixValues(atZ),
        gViewMatrixValues(upX),
        gViewMatrixValues(upY),
        gViewMatrixValues(upZ)
      ).toFloat32Array
    )
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
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

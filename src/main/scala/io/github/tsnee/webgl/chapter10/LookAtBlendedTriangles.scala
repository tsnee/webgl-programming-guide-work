package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom
import org.scalajs.dom.{Element => _, _}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray._

object LookAtBlendedTriangles:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_ProjMatrix;
uniform mat4 u_ViewMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_ProjMatrix * u_ViewMatrix * a_Position;
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

  private val viewEyeX = Var[Float](0.20)
  private val viewEyeY = Var[Float](0.25)
  private val viewEyeZ = 0.25f
  private val viewAtX  = 0f
  private val viewAtY  = 0f
  private val viewAtZ  = 0f
  private val viewUpX  = 0f
  private val viewUpY  = 1f
  private val viewUpZ  = 0f

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    gl.enable(WebGLRenderingContext.BLEND)
    gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA)
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    val verticesColors = Float32Array(js.Array[Float](
      0.0, 0.5, -0.4, 0.4, 1.0, 0.4, 0.4, // The back green triangle
      -0.5, -0.5, -0.4, 0.4, 1.0, 0.4, 0.4,
      0.5, -0.5, -0.4, 1.0, 0.4, 0.4, 0.4,
      0.5, 0.4, -0.2, 1.0, 0.4, 0.4, 0.4, // The middle yellow triangle
      -0.5, 0.4, -0.2, 1.0, 1.0, 0.4, 0.4,
      0.0, -0.6, -0.2, 1.0, 1.0, 0.4, 0.4,
      0.0, 0.5, 0.0, 0.4, 0.4, 1.0, 0.4,  // The front blue triangle
      -0.5, -0.5, 0.0, 0.4, 0.4, 1.0, 0.4,
      0.5, -0.5, 0.0, 1.0, 0.4, 0.4, 0.4
    ))
    initializeVbo(gl, verticesColors)
    enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 7, 0)
    enableFloatAttribute(gl, program, "a_Color", 4, floatSize * 7, floatSize * 3)
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uViewMatrix    = gl.getUniformLocation(program, "u_ViewMatrix")
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setLookAt(
        eyeX = viewEyeX.now(),
        eyeY = viewEyeY.now(),
        eyeZ = viewEyeZ,
        atX = viewAtX,
        atY = viewAtY,
        atZ = viewAtZ,
        upX = viewUpX,
        upY = viewUpY,
        upZ = viewUpZ
      ).toFloat32Array
    )
    val uProjMatrix    = gl.getUniformLocation(program, "u_ProjMatrix")
    gl.uniformMatrix4fv(
      location = uProjMatrix,
      transpose = false,
      value = Matrix4.setOrtho(left = -1, right = 1, bottom = -1, top = 1, near = 0, far = 2).toFloat32Array
    )
    val numVertices    = verticesColors.size / 7
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
  )(
      evt: KeyboardEvent
  ): Unit =
    evt.preventDefault()
    evt.key match
      case KeyValue.ArrowLeft | "h"  => viewEyeX.update(_ - 0.01f)
      case KeyValue.ArrowRight | "l" => viewEyeX.update(_ + 0.01f)
      case KeyValue.ArrowDown | "j"  => viewEyeY.update(_ - 0.01f)
      case KeyValue.ArrowUp | "k"    => viewEyeY.update(_ + 0.01f)
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setLookAt(
        eyeX = viewEyeX.now(),
        eyeY = viewEyeY.now(),
        eyeZ = viewEyeZ,
        atX = viewAtX,
        atY = viewAtY,
        atZ = viewAtZ,
        upX = viewUpX,
        upY = viewUpY,
        upZ = viewUpZ
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

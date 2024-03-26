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
import org.scalajs.dom.KeyValue
import org.scalajs.dom.KeyboardEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLUniformLocation

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object LookAtTrianglesWithKeys:
  val vertexShaderSource: VertexShaderSource =
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

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  private val eyeX = Var[Float](0.20)
  private val eyeY = Var[Float](0.25)

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    val verticesColors = Float32Array(js.Array[Float](
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
    val uViewMatrix    = gl.getUniformLocation(program, "u_ViewMatrix")
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setLookAt(
        eyeX.now(),
        eyeY.now(),
        0.25,
        0,
        0,
        0,
        0,
        1,
        0
      ).toFloat32Array
    )
    val numVertices    = verticesColors.size / 6
    // tabindex must be set to allow an HTML Canvas to receive keyboard input
    canvas.amend(tabIndex := 0, onKeyDown --> keyDown(gl, numVertices, uViewMatrix))
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
      case KeyValue.ArrowLeft | "h"  => eyeX.update(_ - 0.01f)
      case KeyValue.ArrowRight | "l" => eyeX.update(_ + 0.01f)
      case KeyValue.ArrowDown | "j"  => eyeY.update(_ - 0.01f)
      case KeyValue.ArrowUp | "k"    => eyeY.update(_ + 0.01f)
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setLookAt(
        eyeX.now(),
        eyeY.now(),
        0.25,
        0,
        0,
        0,
        0,
        1,
        0
      ).toFloat32Array
    )
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )

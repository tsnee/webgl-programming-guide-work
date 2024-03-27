package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object PerspectiveView:
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

  private val viewEyeX = Var[Float](0)
  private val viewEyeY = Var[Float](0)
  private val viewEyeZ = 5f
  private val viewAtX  = 0f
  private val viewAtY  = 0f
  private val viewAtZ  = -100f
  private val viewUpX  = 0f
  private val viewUpY  = 1f
  private val viewUpZ  = 0f

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    val verticesColors = Float32Array(js.Array(
      // Three triangles on the right side
      0.75f, 1.0f, -4.0f, 0.4f, 1.0f, 0.4f, // The back green one
      0.25f, -1.0f, -4.0f, 0.4f, 1.0f, 0.4f,
      1.25f, -1.0f, -4.0f, 1.0f, 0.4f, 0.4f,
      0.75f, 1.0f, -2.0f, 1.0f, 1.0f, 0.4f, // The middle yellow one
      0.25f, -1.0f, -2.0f, 1.0f, 1.0f, 0.4f,
      1.25f, -1.0f, -2.0f, 1.0f, 0.4f, 0.4f,
      0.75f, 1.0f, 0.0f, 0.4f, 0.4f, 1.0f,  // The front blue one
      0.25f, -1.0f, 0.0f, 0.4f, 0.4f, 1.0f,
      1.25f, -1.0f, 0.0f, 1.0f, 0.4f, 0.4f,

      // Three triangles on the left side
      -0.75f, 1.0f, -4.0f, 0.4f, 1.0f, 0.4f, // The back green one
      -1.25f, -1.0f, -4.0f, 0.4f, 1.0f, 0.4f,
      -0.25f, -1.0f, -4.0f, 1.0f, 0.4f, 0.4f,
      -0.75f, 1.0f, -2.0f, 1.0f, 1.0f, 0.4f, // The middle yellow one
      -1.25f, -1.0f, -2.0f, 1.0f, 1.0f, 0.4f,
      -0.25f, -1.0f, -2.0f, 1.0f, 0.4f, 0.4f,
      -0.75f, 1.0f, 0.0f, 0.4f, 0.4f, 1.0f,  // The front blue one
      -1.25f, -1.0f, 0.0f, 0.4f, 0.4f, 1.0f,
      -0.25f, -1.0f, 0.0f, 1.0f, 0.4f, 0.4f
    ))
    VertexBufferObject.initializeVbo(gl, verticesColors)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 3, floatSize * 6, 0)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.clearColor(0f, 0f, 0f, 1f)
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
      value = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100).toFloat32Array
    )
    val numVertices    = verticesColors.size / 6
    canvas.amend(onKeyDown --> keyDown(gl, numVertices, uViewMatrix))
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

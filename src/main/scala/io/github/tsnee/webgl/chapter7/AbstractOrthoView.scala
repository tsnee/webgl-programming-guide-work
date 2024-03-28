package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ExerciseBuilder
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

abstract class AbstractOrthoView:
  def orthoProjectionLeft: Float
  def orthoProjectionRight: Float
  def orthoProjectionBottom: Float
  def orthoProjectionTop: Float

  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_ProjMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_ProjMatrix * a_Position;
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

  private val orthoProjectionNear = Var[Float](0)
  private val orthoProjectionFar  = Var[Float](0.5)
  private val nearFarText         = Var[String](f"near: ${orthoProjectionNear.now()}%.2f, far: ${orthoProjectionFar.now()}%.2f")

  def panel(canvasHeight: Height, canvasWidth: Width): Element =
    val component = ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(
      canvasHeight,
      canvasWidth
    )
    component.amend(p(child.text <-- nearFarText.signal))

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    // These vertex values aren't in the book, only the book's source code.
    val verticesColors = Float32Array(js.Array[Float](
      0.0, 0.6, -0.4, 0.4, 1.0, 0.4, // The back green one
      -0.5, -0.4, -0.4, 0.4, 1.0, 0.4,
      0.5, -0.4, -0.4, 1.0, 0.4, 0.4,
      0.5, 0.4, -0.2, 1.0, 0.4, 0.4, // The middle yellow one
      -0.5, 0.4, -0.2, 1.0, 1.0, 0.4,
      0.0, -0.6, -0.2, 1.0, 1.0, 0.4,
      0.0, 0.5, 0.0, 0.4, 0.4, 1.0,  // The front blue one
      -0.5, -0.5, 0.0, 0.4, 0.4, 1.0,
      0.5, -0.5, 0.0, 1.0, 0.4, 0.4
    ))
    VertexBufferObject.initializeVbo(gl, verticesColors)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 3, floatSize * 6, 0)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uProjMatrix    = gl.getUniformLocation(program, "u_ProjMatrix")
    gl.uniformMatrix4fv(
      location = uProjMatrix,
      transpose = false,
      value = Matrix4.setOrtho(
        orthoProjectionLeft,
        orthoProjectionRight,
        orthoProjectionBottom,
        orthoProjectionTop,
        orthoProjectionNear.now(),
        orthoProjectionFar.now()
      ).toFloat32Array
    )
    val numVertices    = verticesColors.size / 6
    canvas.amend(tabIndex := 0, onKeyDown --> keyDown(gl, numVertices, uProjMatrix))
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
      case KeyValue.ArrowLeft | "h"  => orthoProjectionNear.update(_ - 0.01f)
      case KeyValue.ArrowRight | "l" => orthoProjectionNear.update(_ + 0.01f)
      case KeyValue.ArrowDown | "j"  => orthoProjectionFar.update(_ - 0.01f)
      case KeyValue.ArrowUp | "k"    => orthoProjectionFar.update(_ + 0.01f)
      case _                         => ()
    nearFarText.set(f"near: ${orthoProjectionNear.now()}%.2f, far: ${orthoProjectionFar.now()}%.2f")
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setOrtho(
        orthoProjectionLeft,
        orthoProjectionRight,
        orthoProjectionBottom,
        orthoProjectionTop,
        orthoProjectionNear.now(),
        orthoProjectionFar.now()
      ).toFloat32Array
    )
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.drawArrays(
      mode = WebGLRenderingContext.TRIANGLES,
      first = 0,
      count = numVertices
    )

package io.github.tsnee.webgl.chapter7

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object OrthoViewHalfWidth extends Exercise:
  override val label: String = "OrthoView_halfWidth"

  override val height: Int = 500

  override def build: Element =
    val component = document.createElement("div")
    component.setAttribute("id", Exercise.componentId)
    document.createElement("canvas") match
      case canvas: HTMLCanvasElement =>
        canvas.height = Exercise.canvasHeight
        canvas.width = Exercise.canvasWidth
        canvas.setAttribute("tabindex", "0") // make the canvas capable of receiving focus
        component.appendChild(canvas)
        val p = document.createElement("p")
        p.innerText = "The near and far values are displayed here."
        p.setAttribute("id", labelParagraphId)
        component.appendChild(p)
        initialize(canvas)
        component

  private val labelParagraphId = "nearFar"

  val vertexShaderSource: String =
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

  val fragmentShaderSource: String =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  private val gProjectionMatrixValues               = Array.ofDim[Float](6)
  // indices into gProjectionMatrixValues
  private val (left, right, bottom, top, near, far) = (0, 1, 2, 3, 4, 5)

  def initialize(canvas: Canvas): Unit =
    Array(-0.3f, 0.3f, -1f, 1f, 0f, 0.5f).copyToArray(gProjectionMatrixValues)
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run(canvas, _, _)
    )

  private def run(
      canvas: HTMLCanvasElement,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    // These values aren't in the book, only the book's source code.
    val verticesColors = Float32Array(js.Array(
      0.0f, 0.6f, -0.4f, 0.4f, 1.0f, 0.4f, // The back green one
      -0.5f, -0.4f, -0.4f, 0.4f, 1.0f, 0.4f,
      0.5f, -0.4f, -0.4f, 1.0f, 0.4f, 0.4f,
      0.5f, 0.4f, -0.2f, 1.0f, 0.4f, 0.4f, // The middle yellow one
      -0.5f, 0.4f, -0.2f, 1.0f, 1.0f, 0.4f,
      0.0f, -0.6f, -0.2f, 1.0f, 1.0f, 0.4f,
      0.0f, 0.5f, 0.0f, 0.4f, 0.4f, 1.0f,  // The front blue one
      -0.5f, -0.5f, 0.0f, 0.4f, 0.4f, 1.0f,
      0.5f, -0.5f, 0.0f, 1.0f, 0.4f, 0.4f
    ))
    initializeVbo(gl, verticesColors)
    enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val uProjMatrix    = gl.getUniformLocation(program, "u_ProjMatrix")
    gl.uniformMatrix4fv(
      location = uProjMatrix,
      transpose = false,
      value = Matrix4.setOrtho(
        gProjectionMatrixValues(left),
        gProjectionMatrixValues(right),
        gProjectionMatrixValues(bottom),
        gProjectionMatrixValues(top),
        gProjectionMatrixValues(near),
        gProjectionMatrixValues(far)
      ).toFloat32Array
    )
    val numVertices    = verticesColors.size / 6
    canvas.addEventListener("keydown", keyDown(gl, numVertices, uProjMatrix)(_))
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
      case KeyValue.ArrowLeft | "h"  => gProjectionMatrixValues(near) -= 0.01f
      case KeyValue.ArrowRight | "l" => gProjectionMatrixValues(near) += 0.01f
      case KeyValue.ArrowDown | "j"  => gProjectionMatrixValues(far) -= 0.01f
      case KeyValue.ArrowUp | "k"    => gProjectionMatrixValues(far) += 0.01f
      case _                         => ()
    document.querySelector(s"p#$labelParagraphId").innerText =
      f"near: ${gProjectionMatrixValues(near)}%.2f, far: ${gProjectionMatrixValues(far)}%.2f"
    gl.uniformMatrix4fv(
      location = uViewMatrix,
      transpose = false,
      value = Matrix4.setOrtho(
        gProjectionMatrixValues(left),
        gProjectionMatrixValues(right),
        gProjectionMatrixValues(bottom),
        gProjectionMatrixValues(top),
        gProjectionMatrixValues(near),
        gProjectionMatrixValues(far)
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

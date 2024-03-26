package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Element => _, Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object PickObject extends Exercise:
  override val label: String = "PickObject"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  val vertexShaderSource: String =
    """
attribute vec4 a_Color;
attribute vec4 a_Position;
uniform mat4 u_MvpMatrix;
uniform bool u_Clicked;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  if (u_Clicked) {
    v_Color = vec4(1.0, 0.0, 0.0, 1.0);
  } else {
    v_Color = a_Color;
  }
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

  val gCurrentAngle: Array[Float] = Array.ofDim[Float](1)

  def initialize(canvas: Canvas): Unit =
    gCurrentAngle(0) = 0
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run(canvas, _, _)
    )

  private def run(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val vertices       = Float32Array(js.Array[Float](
      1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,
      1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,
      1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,
      -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1,
      -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1,
      1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1
    ))
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    val colors         = Float32Array(js.Array[Float](
      0.2, 0.58, 0.82, 0.2, 0.58, 0.82, 0.2, 0.58, 0.82, 0.2, 0.58, 0.82,     // v0-v1-v2-v3 front
      0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69,     // v0-v3-v4-v5 right
      0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61,     // v0-v5-v6-v1 up
      0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, // v1-v6-v7-v2 left
      0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, // v7-v4-v3-v2 down
      0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, colors, 3, WebGLRenderingContext.FLOAT, "a_Color")
    // Indices of the vertices
    val indices        = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3,       // front
      4, 5, 6, 4, 6, 7,       // right
      8, 9, 10, 8, 10, 11,    // up
      12, 13, 14, 12, 14, 15, // left
      16, 17, 18, 16, 18, 19, // down
      20, 21, 22, 20, 22, 23  // back
    ))
    val indexBuffer    = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val projMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 3f, eyeY = 3f, eyeZ = 7f, atX = 0f, atY = 0f, atZ = 0f, upX = 0f, upY = 1f, upZ = 0f)
    val viewProjMatrix = projMatrix * viewMatrix
    val uClicked       = gl.getUniformLocation(program, "u_Clicked")
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    registerMouseListeners(canvas, gl, indices.length, uClicked, viewProjMatrix, uMvpMatrix)
    val startTs        = js.Date.now()
    tick(gl, indices.length, uMvpMatrix, viewProjMatrix, startTs)(startTs)

  private def registerMouseListeners(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      numIndices: Int,
      uClicked: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Unit =
    canvas.addEventListener(
      "mousedown",
      (ev: MouseEvent) =>
        val mouseX = ev.clientX
        val mouseY = ev.clientY
        val rect   = ev.target match
          case elem: Element => elem.getBoundingClientRect()
          case _             => new DOMRect
        if rect.left <= mouseX && mouseX < rect.right &&
          rect.top <= mouseY && mouseY < rect.bottom
        then
          val xInCanvas = mouseX - rect.left
          val yInCanvas = rect.bottom - mouseY
          val picked    =
            check(gl, numIndices, xInCanvas.toInt, yInCanvas.toInt, uClicked, viewProjMatrix, uMvpMatrix)
          if picked then window.alert("The cube was selected!")
    )

  private def check(
      gl: WebGLRenderingContext,
      numIndices: Int,
      x: Int,
      y: Int,
      uClicked: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Boolean =
    gl.uniform1i(uClicked, 1)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix) // cube will be red
    val pixels = Uint8Array(4)
    gl.readPixels(x, y, 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixels)
    val picked = pixels(0) === 255 // pixel under mouse is red
    gl.uniform1i(uClicked, 0)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix) // cube will be its normal color
    picked

  private def tick(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    animate(prevTs, curTs)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix)
    val _ = window.requestAnimationFrame(tick(gl, numIndices, uMvpMatrix, viewProjMatrix, curTs)(_))

  private val angleStepDegrees = 20f

  private def animate(prevTs: Double, curTs: Double): Unit =
    gCurrentAngle(0) = (gCurrentAngle(0) + (angleStepDegrees * (curTs - prevTs)).toFloat / 1000) % 360

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  ): Unit =
    val mvpMatrix = viewProjMatrix
      .rotate(gCurrentAngle(0), 1, 0, 0)
      .rotate(gCurrentAngle(0), 0, 1, 0)
      .rotate(gCurrentAngle(0), 0, 0, 1)
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = numIndices,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )

  private def initializeVbo(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      array: Float32Array,
      size: Int,
      typ: Int,
      attributeName: String
  ): Unit =
    val vertexBufferObject = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexBufferObject)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, array, WebGLRenderingContext.STATIC_DRAW)
    val attribute          = gl.getAttribLocation(program, attributeName)
    gl.vertexAttribPointer(
      indx = attribute,
      size = size,
      `type` = typ,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(attribute)

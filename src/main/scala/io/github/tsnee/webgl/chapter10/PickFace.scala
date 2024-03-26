package io.github.tsnee.webgl.chapter10

import com.raquo.laminar.api.L.{Element => _, Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._

import scala.scalajs.js
import scala.scalajs.js.typedarray.ArrayBufferView
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object PickFace extends Exercise:
  override def label: String = "PickFace"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  private val vertexShaderSource = """
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute float a_Face;
uniform mat4 u_MvpMatrix;
uniform int u_PickedFace;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  int face = int(a_Face);
  vec3 color = (face == u_PickedFace) ? vec3(1.0) : a_Color.rgb;
  if (u_PickedFace == 0) {
    v_Color = vec4(color, a_Face / 255.0);
  } else {
    v_Color = vec4(color, a_Color.a);
  }
}
"""

  private val fragmentShaderSource = """
precision highp float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  val gCurrentAngle: Array[Float] = Array.ofDim[Float](1)

  override def initialize(canvas: HTMLCanvasElement): Unit =
    gCurrentAngle(0) = 0
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run(canvas, _, _)
    )

  private def run(canvas: HTMLCanvasElement, gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val numIndices     = initVertexBuffers(gl, program)
    gl.clearColor(0, 0, 0, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val uPickedFace    = gl.getUniformLocation(program, "u_PickedFace")
    gl.uniform1i(uPickedFace, -1)
    val viewProjMatrix = Matrix4
      .setPerspective(30, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
      .lookAt(0, 0, 7, 0, 0, 0, 0, 1, 0)
    canvas.addEventListener(
      "mousedown",
      (ev: MouseEvent) =>
        val x    = ev.clientX
        val y    = ev.clientY
        val rect = ev.target match
          case elem: Element => elem.getBoundingClientRect()
        if rect.left <= x && x < rect.right && rect.top <= y && y < rect.bottom
        then
          val xInCanvas = (x - rect.left).toInt
          val yInCanvas = (rect.bottom - y).toInt
          val face      = checkFace(gl, numIndices, xInCanvas, yInCanvas, uPickedFace, viewProjMatrix, uMvpMatrix)
          gl.uniform1i(uPickedFace, face)
          draw(gl, numIndices, viewProjMatrix, uMvpMatrix)
    )
    val ts             = js.Date.now()
    val _              = tick(gl, numIndices, viewProjMatrix, uMvpMatrix, ts)(ts)

  private def tick(
      gl: WebGLRenderingContext,
      numIndices: Int,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation,
      prevTs: Double
  )(
      curTs: Double
  ): Int =
    animate(prevTs, curTs)
    draw(gl, numIndices, viewProjMatrix, uMvpMatrix)
    window.requestAnimationFrame(tick(gl, numIndices, viewProjMatrix, uMvpMatrix, curTs)(_))

  private def animate(prevTs: Double, curTs: Double): Unit =
    val elapsedMillis = curTs - prevTs
    gCurrentAngle(0) = (gCurrentAngle(0) + (20 * elapsedMillis).toFloat / 1000) % 360

  private def initVertexBuffers(gl: WebGLRenderingContext, program: WebGLProgram): Int =
    val vertices    = Float32Array(js.Array[Float](
      1.0, 1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0, 1.0,     // v0-v1-v2-v3 front
      1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 1.0, -1.0, -1.0, 1.0, 1.0, -1.0,     // v0-v3-v4-v5 right
      1.0, 1.0, 1.0, 1.0, 1.0, -1.0, -1.0, 1.0, -1.0, -1.0, 1.0, 1.0,     // v0-v5-v6-v1 up
      -1.0, 1.0, 1.0, -1.0, 1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 1.0, // v1-v6-v7-v2 left
      -1.0, -1.0, -1.0, 1.0, -1.0, -1.0, 1.0, -1.0, 1.0, -1.0, -1.0, 1.0, // v7-v4-v3-v2 down
      1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, 1.0, -1.0, 1.0, 1.0, -1.0  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    val colors      = Float32Array(js.Array[Float](
      0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, // v0-v1-v2-v3 front
      0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69,     // v0-v3-v4-v5 right
      0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, // v0-v5-v6-v1 up
      0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61,     // v1-v6-v7-v2 left
      0.27, 0.58, 0.82, 0.27, 0.58, 0.82, 0.27, 0.58, 0.82, 0.27, 0.58, 0.82, // v7-v4-v3-v2 down
      0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, colors, 3, WebGLRenderingContext.FLOAT, "a_Color")
    val jsFaces     =
      for
        face <- 1 to 6
        _    <- 0 until 4
      yield face.toShort
    val faces       = Uint8Array(js.Array[Short](jsFaces*))
    initializeVbo(gl, program, faces, 1, WebGLRenderingContext.UNSIGNED_BYTE, "a_Face")
    val indices     = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3,       // front
      4, 5, 6, 4, 6, 7,       // right
      8, 9, 10, 8, 10, 11,    // up
      12, 13, 14, 12, 14, 15, // left
      16, 17, 18, 16, 18, 19, // down
      20, 21, 22, 20, 22, 23  // back
    ))
    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    indices.length

  private def checkFace(
      gl: WebGLRenderingContext,
      numIndices: Int,
      x: Int,
      y: Int,
      uPickedFace: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Int =
    val pixels = Uint8Array(4)
    gl.uniform1i(uPickedFace, 0)
    draw(gl, numIndices, viewProjMatrix, uMvpMatrix)
    gl.readPixels(x, y, 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixels)
    pixels(3) // alpha channel

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Unit =
    val mvpMatrix = viewProjMatrix
      .rotate(gCurrentAngle(0), 1, 0, 0)
      .rotate(gCurrentAngle(0), 0, 1, 0)
      .rotate(gCurrentAngle(0), 0, 0, 1)
    gl.uniformMatrix4fv(uMvpMatrix, transpose = false, mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(WebGLRenderingContext.TRIANGLES, numIndices, WebGLRenderingContext.UNSIGNED_BYTE, offset = 0)

  private def initializeVbo(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      array: ArrayBufferView,
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

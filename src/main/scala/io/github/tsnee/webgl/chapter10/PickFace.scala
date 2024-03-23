package io.github.tsnee.webgl.chapter10

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.ArrayBufferView
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object PickFace extends Exercise:
  override val label: String = "PickFace"

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute float a_Face;
uniform mat4 u_MvpMatrix;
uniform int u_PickedFace;
// expected u_PickedFace values:
//  -1: program just started
//   0: mouse is clicked
// 1-6: face last clicked
// 255: background last clicked
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  int face = int(a_Face);
  vec3 color = (face == u_PickedFace) ? vec3(1.0) : a_Color.rgb;
  if (u_PickedFace == 0) {  // mouse was just clicked
    v_Color = vec4(a_Color.rgb, a_Face / 255.0);
  } else {
    v_Color = vec4(color, a_Color.a);
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

  private val angleStepDegrees = 20

  def initialize(canvas: Canvas): Unit =
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
    gl.useProgram(program)

    val numIndices     = setupBuffers(gl, program)

    gl.clearColor(0f, 0f, 0f, 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)

    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val uPickedFace    = gl.getUniformLocation(program, "u_PickedFace")

    val viewProjMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
      .lookAt(eyeX = 0, eyeY = 0, eyeZ = 7, atX = 0, atY = 0, atZ = 0, upX = 0, upY = 1, upZ = 0)

    gl.uniform1i(uPickedFace, -1)

    val currentAngle = 0f
    registerMouseListeners(canvas, gl, numIndices, currentAngle, uPickedFace, viewProjMatrix, uMvpMatrix)

    val startTs        = js.Date.now()
    tick(gl, numIndices, currentAngle, uMvpMatrix, viewProjMatrix, startTs)(startTs)

  private def setupBuffers(gl: WebGLRenderingContext, program: WebGLProgram): Int =
    val vertices = Float32Array(js.Array[Float](
      1.0, 1.0, 1.0,  -1.0, 1.0, 1.0,  -1.0,-1.0, 1.0,   1.0,-1.0, 1.0,    // v0-v1-v2-v3 front
      1.0, 1.0, 1.0,   1.0,-1.0, 1.0,   1.0,-1.0,-1.0,   1.0, 1.0,-1.0,    // v0-v3-v4-v5 right
      1.0, 1.0, 1.0,   1.0, 1.0,-1.0,  -1.0, 1.0,-1.0,  -1.0, 1.0, 1.0,    // v0-v5-v6-v1 up
      -1.0, 1.0, 1.0,  -1.0, 1.0,-1.0,  -1.0,-1.0,-1.0,  -1.0,-1.0, 1.0,    // v1-v6-v7-v2 left
      -1.0,-1.0,-1.0,   1.0,-1.0,-1.0,   1.0,-1.0, 1.0,  -1.0,-1.0, 1.0,    // v7-v4-v3-v2 down
      1.0,-1.0,-1.0,  -1.0,-1.0,-1.0,  -1.0, 1.0,-1.0,   1.0, 1.0,-1.0     // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    val colors = Float32Array(js.Array[Float](
      0.32, 0.18, 0.56,  0.32, 0.18, 0.56,  0.32, 0.18, 0.56,  0.32, 0.18, 0.56, // v0-v1-v2-v3 front
      0.5, 0.41, 0.69,   0.5, 0.41, 0.69,   0.5, 0.41, 0.69,   0.5, 0.41, 0.69,  // v0-v3-v4-v5 right
      0.78, 0.69, 0.84,  0.78, 0.69, 0.84,  0.78, 0.69, 0.84,  0.78, 0.69, 0.84, // v0-v5-v6-v1 up
      0.0, 0.32, 0.61,   0.0, 0.32, 0.61,   0.0, 0.32, 0.61,   0.0, 0.32, 0.61,  // v1-v6-v7-v2 left
      0.27, 0.58, 0.82,  0.27, 0.58, 0.82,  0.27, 0.58, 0.82,  0.27, 0.58, 0.82, // v7-v4-v3-v2 down
      0.73, 0.82, 0.93,  0.73, 0.82, 0.93,  0.73, 0.82, 0.93,  0.73, 0.82, 0.93, // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, colors, 3, WebGLRenderingContext.FLOAT, "a_Color")
    val faces = Uint8Array(js.Array[Short](
      1, 1, 1, 1,
      2, 2, 2, 2,
      3, 3, 3, 3,
      4, 4, 4, 4,
      5, 5, 5, 5,
      6, 6, 6, 6
    ))
    initializeVbo(gl, program, faces, 1, WebGLRenderingContext.UNSIGNED_BYTE, "a_Face")
    // Indices of the vertices
    val indices = Uint8Array(js.Array[Short](
      0, 1, 2,   0, 2, 3,    // front
      4, 5, 6,   4, 6, 7,    // right
      8, 9,10,   8,10,11,    // up
      12,13,14,  12,14,15,    // left
      16,17,18,  16,18,19,    // down
      20,21,22,  20,22,23     // back
    ))
    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    indices.length

  private def registerMouseListeners(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      numIndices: Int,
      currentAngle: Float,
      uPickedFace: WebGLUniformLocation,
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
        if rect.left <= mouseX && mouseX < rect.right &&
          rect.top <= mouseY && mouseY < rect.bottom
        then
          val xInCanvas = mouseX - rect.left
          val yInCanvas = rect.bottom - mouseY
          val face      =
            checkFace(
              gl,
              numIndices,
              xInCanvas.toInt,
              yInCanvas.toInt,
              currentAngle,
              uPickedFace,
              viewProjMatrix,
              uMvpMatrix
            )
          gl.uniform1i(uPickedFace, face)
          draw(gl, numIndices, currentAngle, viewProjMatrix, uMvpMatrix)
    )

  private def checkFace(
      gl: WebGLRenderingContext,
      numIndices: Int,
      x: Int,
      y: Int,
      currentAngle: Float,
      uPickedFace: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Int =
    val pixels = Uint8Array(4)
    gl.uniform1i(uPickedFace, 0)
    draw(gl, numIndices, currentAngle, viewProjMatrix, uMvpMatrix)
    gl.readPixels(x, y, 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixels)
    console.log(s"readPixels($x, $y, ...) returns $pixels")
    pixels(3) // each face has its number encoded in its alpha channel

  private def tick(
      gl: WebGLRenderingContext,
      numIndices: Int,
      currentAngle: Float,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    val nextAngle = animate(currentAngle, prevTs, curTs)
    draw(gl, numIndices, nextAngle, viewProjMatrix, uMvpMatrix)
    val _         = window.requestAnimationFrame(tick(gl, numIndices, nextAngle, uMvpMatrix, viewProjMatrix, curTs)(_))

  private def animate(currentAngle: Float, prevTs: Double, curTs: Double): Float =
    (currentAngle + (angleStepDegrees * (curTs - prevTs).toFloat) / 1000) % 360

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      currentAngle: Float,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation,
  ): Unit =
    val mvpMatrix =
      viewProjMatrix.rotate(currentAngle, 1, 0, 0).rotate(currentAngle, 0, 1, 0).rotate(currentAngle, 0, 0, 1)
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

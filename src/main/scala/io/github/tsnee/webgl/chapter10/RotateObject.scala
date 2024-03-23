package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object RotateObject extends Exercise:
  override val label: String = "RotateObject"

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec2 a_TexCoord;
uniform mat4 u_MvpMatrix;
varying vec2 v_TexCoord;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  v_TexCoord = a_TexCoord;
}
"""

  val fragmentShaderSource: String =
    """
precision mediump float;
uniform sampler2D u_Sampler;
varying vec2 v_TexCoord;
void main() {
  gl_FragColor = texture2D(u_Sampler, v_TexCoord);
}
"""

  private val gCurrentAngleDegrees = Array.ofDim[Float](2)
  private val xAxis                = 0 // index into gCurrentAngleDegrees
  private val yAxis                = 1 // index into gCurrentAngleDegrees
  private val gMouseState          = Array.ofDim[Double](3)
  private val dragging             = 0 // index into gMouseState
  private val lastX                = 1 // index into gMouseState
  private val lastY                = 2 // index into gMouseState

  def initialize(canvas: Canvas): Unit =
    Array[Float](0, 0).copyToArray(gCurrentAngleDegrees)
    Array[Double](0, 0).copyToArray(gMouseState)
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
    val texCoords      = Float32Array(js.Array[Float](
      1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, // v0-v1-v2-v3 front
      0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, // v0-v3-v4-v5 right
      1.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, // v0-v5-v6-v1 up
      1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, // v1-v6-v7-v2 left
      0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // v7-v4-v3-v2 down
      0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, texCoords, 2, WebGLRenderingContext.FLOAT, "a_TexCoord")
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
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    registerMouseListeners(canvas)
    loadTextures(gl, program)
    tick(gl, indices.length, uMvpMatrix, viewProjMatrix)(js.Date.now())

  private def loadTextures(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val tex      = gl.createTexture()
    val uSampler = gl.getUniformLocation(program, "u_Sampler")
    val image    = Image()
    image.addEventListener("load", textureLoaded(gl, tex, image, uSampler)(_))
    image.src = "sky.jpg"

  private def textureLoaded(
      gl: WebGLRenderingContext,
      tex: WebGLTexture,
      image: Image,
      uSampler: WebGLUniformLocation
  )(
      @unused event: Event
  ): Unit =
    gl.pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1)
    gl.activeTexture(WebGLRenderingContext.TEXTURE0)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, tex)
    gl.texParameteri(
      WebGLRenderingContext.TEXTURE_2D,
      WebGLRenderingContext.TEXTURE_MIN_FILTER,
      WebGLRenderingContext.LINEAR
    )
    gl.texImage2D(
      WebGLRenderingContext.TEXTURE_2D,
      0,
      WebGLRenderingContext.RGB,
      WebGLRenderingContext.RGB,
      WebGLRenderingContext.UNSIGNED_BYTE,
      image
    )
    gl.uniform1i(uSampler, 0)

  private def registerMouseListeners(canvas: Canvas): Unit =
    canvas.addEventListener(
      "mousedown",
      (ev: MouseEvent) =>
        gMouseState(dragging) = 1
        val mouseX = ev.clientX
        val mouseY = ev.clientY
        val rect   = ev.target match
          case elem: Element => elem.getBoundingClientRect()
          case _             => new DOMRect
        if rect.left <= mouseX && mouseX < rect.right &&
          rect.top <= mouseY && mouseY < rect.top
        then
          gMouseState(lastX) = mouseX.toInt
          gMouseState(lastY) = mouseY.toInt
    )
    canvas.addEventListener("mouseup", (_: MouseEvent) => gMouseState(dragging) = 0)
    canvas.addEventListener(
      "mousemove",
      (ev: MouseEvent) =>
        val mouseX = ev.clientX
        val mouseY = ev.clientY
        if gMouseState(dragging) =!= 0
        then
          val rotationRatio = 100.0 / canvas.height
          val dx            = rotationRatio * (mouseX - gMouseState(lastX))
          val dy            = rotationRatio * (mouseY - gMouseState(lastY))
          gCurrentAngleDegrees(xAxis) = Math.max(Math.min(gCurrentAngleDegrees(0) + dy, 90), -90).toFloat
          gCurrentAngleDegrees(yAxis) += dx.toFloat
        gMouseState(lastX) = mouseX
        gMouseState(lastY) = mouseY
    )

  private def tick(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  )(
      @unused timestamp: Double
  ): Unit =
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix)
    val _ = window.requestAnimationFrame(tick(gl, numIndices, uMvpMatrix, viewProjMatrix)(_))

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  ): Unit =
    val mvpMatrix =
      viewProjMatrix.rotate(gCurrentAngleDegrees(xAxis), 1, 0, 0).rotate(gCurrentAngleDegrees(yAxis), 0, 1, 0)
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

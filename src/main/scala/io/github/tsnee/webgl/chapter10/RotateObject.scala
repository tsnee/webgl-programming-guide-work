package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom
import org.scalajs.dom.{Element => _, _}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray._

object RotateObject:
  val vertexShaderSource: VertexShaderSource =
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

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
uniform sampler2D u_Sampler;
varying vec2 v_TexCoord;
void main() {
  gl_FragColor = texture2D(u_Sampler, v_TexCoord);
}
"""

  private val xAxisRotationDegrees = Var[Float](0)
  private val yAxisRotationDegrees = Var[Float](0)
  private val dragging             = Var[Boolean](false)
  private val lastMousePosition    = Var[(Double, Double)]((0.0, 0.0))

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
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
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Position", size = 3, stride = 0, offset = 0)
    val texCoords      = Float32Array(js.Array[Float](
      1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, // v0-v1-v2-v3 front
      0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, // v0-v3-v4-v5 right
      1.0, 0.0, 1.0, 1.0, 0.0, 1.0, 0.0, 0.0, // v0-v5-v6-v1 up
      1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, // v1-v6-v7-v2 left
      0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0, // v7-v4-v3-v2 down
      0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0  // v4-v7-v6-v5 back
    ))
    VertexBufferObject.initializeVbo(gl, texCoords)
    WebglAttribute.enableFloatAttribute(gl, program, "a_TexCoord", size = 2, stride = 0, offset = 0)
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
    gl.clearColor(0, 0, 0, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val projMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 3, eyeY = 3, eyeZ = 7, atX = 0, atY = 0, atZ = 0, upX = 0, upY = 1, upZ = 0)
    val viewProjMatrix = projMatrix * viewMatrix
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    canvas.amend(
      onMouseDown --> mouseDown,
      onMouseUp --> mouseUp,
      onMouseMove --> mouseMove(100.0 / gl.drawingBufferWidth, 100.0 / gl.drawingBufferHeight)
    )
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

  private def mouseDown(ev: MouseEvent): Unit =
    val mouseX = ev.clientX
    val mouseY = ev.clientY
    val rect   = ev.target match
      case elem: dom.Element => elem.getBoundingClientRect()
      case _                 => new DOMRect
    if rect.left <= mouseX && mouseX < rect.right &&
      rect.top <= mouseY && mouseY < rect.bottom
    then
      dragging.set(true)

  private def mouseUp(@unused evt: MouseEvent): Unit = dragging.set(false)

  private def mouseMove(xRotationRatio: Double, yRotationRatio: Double)(ev: MouseEvent): Unit =
    val mouseX = ev.clientX
    val mouseY = ev.clientY
    if dragging.now()
    then
      val (lastX, lastY) = lastMousePosition.now()
      val dx             = xRotationRatio * (mouseX - lastX)
      val dy             = yRotationRatio * (mouseY - lastY)
      xAxisRotationDegrees.set(Math.max(Math.min(xAxisRotationDegrees.now() + dy, 90), -90).toFloat)
      yAxisRotationDegrees.update(_ + dx.toFloat)
    lastMousePosition.set(mouseX, mouseY)

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
      viewProjMatrix.rotate(xAxisRotationDegrees.now(), 1, 0, 0).rotate(yAxisRotationDegrees.now(), 0, 1, 0)
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = numIndices,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )

package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExerciseBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom
import org.scalajs.dom.{Element => _, _}

import scala.scalajs.js
import scala.scalajs.js.typedarray._

object PickObject:
  val vertexShaderSource: VertexShaderSource =
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

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  private val currentAngle = Var[Float](0)

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(
      canvasWidth: Width,
      canvasHeight: Height
    )

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
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Position",
      size = 3,
      stride = 0,
      offset = 0
    )
    val colors         = Float32Array(js.Array[Float](
      0.2, 0.58, 0.82, 0.2, 0.58, 0.82, 0.2, 0.58, 0.82, 0.2, 0.58, 0.82,     // v0-v1-v2-v3 front
      0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69,     // v0-v3-v4-v5 right
      0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61,     // v0-v5-v6-v1 up
      0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, // v1-v6-v7-v2 left
      0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, // v7-v4-v3-v2 down
      0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93  // v4-v7-v6-v5 back
    ))
    VertexBufferObject.initializeVbo(gl, colors)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Color",
      size = 3,
      stride = 0,
      offset = 0
    )
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
    val uClicked       = gl.getUniformLocation(program, "u_Clicked")
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    canvas.amend(onMouseDown --> mouseDown(gl, indices.length, uClicked, viewProjMatrix, uMvpMatrix))
    val startTs        = js.Date.now()
    tick(gl, indices.length, uMvpMatrix, viewProjMatrix, startTs)(startTs)

  private def mouseDown(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uClicked: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  )(
      evt: MouseEvent
  ): Unit =
    val mouseX = evt.clientX
    val mouseY = evt.clientY
    val rect   = gl.canvas.getBoundingClientRect()
    if rect.left <= mouseX && mouseX < rect.right &&
      rect.top <= mouseY && mouseY < rect.bottom
    then
      val xInCanvas = mouseX - rect.left
      val yInCanvas = rect.bottom - mouseY
      val picked    =
        check(gl, numIndices, xInCanvas.toInt, yInCanvas.toInt, uClicked, viewProjMatrix, uMvpMatrix)
      if picked then window.alert("The cube was selected!")

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
    currentAngle.set((currentAngle.now() + (angleStepDegrees * (curTs - prevTs)).toFloat / 1000) % 360)

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  ): Unit =
    val angle     = currentAngle.now()
    val mvpMatrix = viewProjMatrix
      .rotate(angle, 1, 0, 0)
      .rotate(angle, 0, 1, 0)
      .rotate(angle, 0, 0, 1)
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = numIndices,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )

package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ContextExtractor
import io.github.tsnee.webgl.common.ProgramCreator
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom
import org.scalajs.dom.{Element => _, _}

import scala.scalajs.js
import scala.scalajs.js.typedarray._

object ThreeDOverWeb:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_MvpMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
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

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    val canvas           = canvasTag(
      heightAttr := canvasHeight,
      widthAttr  := canvasWidth,
      position   := "absolute",
      zIndex     := 1,
      left       := "50%",
      top        := "45%",
      transform  := "translate(-50%, -50%)"
    )
    val web              = iframe(
      width    := "100%",
      height   := "100%",
      src      := "https://en.wikipedia.org/wiki/The_Design_of_Design",
      position := "absolute",
      zIndex   := 0
    )
    val successOrFailure =
      for
        gl <- ContextExtractor.extractWebglContext(canvas.ref)
        pg <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
      yield useWebgl(gl, pg)
    successOrFailure match
      case Right(())   => div(position := "relative", width := "100%", height := "100%", canvas, web)
      case Left(error) => div(error)

  private def useWebgl(
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val numIndices     = setupBuffers(gl, program)
    gl.clearColor(0, 0, 0, 0)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val projMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 3f, eyeY = 3f, eyeZ = 7f, atX = 0f, atY = 0f, atZ = 0f, upX = 0f, upY = 1f, upZ = 0f)
    val viewProjMatrix = projMatrix * viewMatrix
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val curTs          = js.Date.now()
    tick(gl, numIndices, uMvpMatrix, viewProjMatrix, curAngle = 0f, curTs)(curTs)

  private def setupBuffers(gl: WebGLRenderingContext, program: WebGLProgram): Int =
    // Create a cube
    //    v6----- v5
    //   /|      /|
    //  v1------v0|
    //  | |     | |
    //  | |v7---|-|v4
    //  |/      |/
    //  v2------v3
    val vertices    = Float32Array(js.Array[Float](
      1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,
      1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,
      1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,
      -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1,
      -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1,
      1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1
    ))
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 3, 0, 0)
    val colors      = Float32Array(js.Array[Float](
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

  private def tick(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      curAngle: Float,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    val nextAngle = animate(curAngle, prevTs, curTs)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix, nextAngle)
    val _         = window.requestAnimationFrame(tick(gl, numIndices, uMvpMatrix, viewProjMatrix, nextAngle, curTs)(_))

  private val angleStepDegrees = 20f

  private def animate(currentAngle: Float, prevTs: Double, curTs: Double): Float =
    (currentAngle + (angleStepDegrees * (curTs - prevTs)).toFloat / 1000) % 360

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      curAngle: Float
  ): Unit =
    val mvpMatrix =
      viewProjMatrix.rotate(curAngle, 1, 0, 0).rotate(curAngle, 0, 1, 0).rotate(curAngle, 0, 0, 1)
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = numIndices,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )

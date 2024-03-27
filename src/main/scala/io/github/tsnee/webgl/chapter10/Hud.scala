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

object Hud:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
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
  private val currentAngle                       = Var[Float](0)

  def panel(canvasHeight: Height, canvasWidth: Width): Element =
    val canvasWebgl      = canvasTag(
      heightAttr := canvasHeight,
      widthAttr  := canvasWidth,
      position   := "absolute",
      zIndex     := 0,
      left       := "50%",
      top        := "45%",
      transform  := "translate(-50%, -50%)"
    )
    val canvasHud        = canvasTag(
      heightAttr := canvasHeight,
      widthAttr  := canvasWidth,
      position   := "absolute",
      zIndex     := 1,
      left       := "50%",
      top        := "45%",
      transform  := "translate(-50%, -50%)"
    )
    val successOrFailure =
      for
        gl  <- ContextExtractor.extractWebglContext(canvasWebgl.ref)
        pg  <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
        ctx <- ContextExtractor.extract2dContext(canvasHud.ref)
      yield useWebglAnd2d(gl, pg, canvasHud, ctx)
    successOrFailure match
      case Right(())   => div(position := "relative", height := "100%", width := "100%", canvasWebgl, canvasHud)
      case Left(error) => div(error)

  private def useWebglAnd2d(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      canvasHud: Canvas,
      ctx: CanvasRenderingContext2D
  ): Unit =
    val numIndices     = setupBuffers(gl, program)
    gl.clearColor(0, 0, 0, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val projMatrix     = Matrix4.setPerspective(30, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 3, eyeY = 3, eyeZ = 7, atX = 0, atY = 0, atZ = 0, upX = 0, upY = 1, upZ = 0)
    val viewProjMatrix = projMatrix * viewMatrix
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val uClicked       = gl.getUniformLocation(program, "u_Clicked")
    gl.uniform1i(uClicked, 0)
    canvasHud.amend(onMouseDown --> mouseDown(gl, numIndices, uClicked, uMvpMatrix, viewProjMatrix))
    val curTs          = js.Date.now()
    tick(gl, ctx, numIndices, uMvpMatrix, viewProjMatrix, curTs)(curTs)

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

  private def mouseDown(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uClicked: WebGLUniformLocation,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  )(ev: MouseEvent): Unit =
    val mouseX = ev.clientX
    val mouseY = ev.clientY
    val rect   = gl.canvas.getBoundingClientRect()
    if rect.left <= mouseX && mouseX < rect.right &&
      rect.top <= mouseY && mouseY < rect.bottom
    then
      val xInCanvas = mouseX - rect.left
      val yInCanvas = rect.bottom - mouseY
      val picked    =
        check(gl, numIndices, uClicked, xInCanvas.toInt, yInCanvas.toInt, uMvpMatrix, viewProjMatrix)
      if picked then window.alert("The cube was selected!")

  private def tick(
      gl: WebGLRenderingContext,
      ctx: CanvasRenderingContext2D,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    animate(prevTs, curTs)
    draw2d(ctx)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix)
    val _ = window.requestAnimationFrame(tick(gl, ctx, numIndices, uMvpMatrix, viewProjMatrix, curTs)(_))

  private val angleStepDegrees = 20

  private def animate(prevTs: Double, curTs: Double): Unit =
    val elapsedMillis = curTs - prevTs
    currentAngle.set((currentAngle.now() + (angleStepDegrees * elapsedMillis).toFloat / 1000) % 360)

  private def draw(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  ): Unit =
    val curAngle  = currentAngle.now()
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

  private def draw2d(ctx: CanvasRenderingContext2D): Unit =
    ctx.clearRect(0, 0, 400, 400)              // Clear <hud>
    // Draw triangle with white lines
    ctx.beginPath()                            // Start drawing
    ctx.moveTo(120, 10); ctx.lineTo(200, 150); ctx.lineTo(40, 150)
    ctx.closePath()
    ctx.strokeStyle = "rgba(255, 255, 255, 1)" // Set white to color of lines
    ctx.stroke()                               // Draw Triangle with white lines
    // Draw white letters
    ctx.font = """18px "Times New Roman""""
    ctx.fillStyle = "rgba(255, 255, 255, 1)"   // Set white to the color of letters
    ctx.fillText("HUD: Head Up Display", 40, 180)
    ctx.fillText("Triangle is drawn by Canvas 2D API.", 40, 200)
    ctx.fillText("Cube is drawn by WebGL API.", 40, 220)
    ctx.fillText("Current Angle: " + Math.floor(currentAngle.now()), 40, 240)

  private def check(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uClicked: WebGLUniformLocation,
      x: Int,
      y: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4
  ): Boolean =
    gl.uniform1i(uClicked, 1); // Pass true to u_Clicked(Draw cube with red)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix);
    // Read pixel at the clicked position
    val pixels = Uint8Array(4); // Array for storing the pixel value
    gl.readPixels(x, y, 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixels);
    gl.uniform1i(uClicked, 0); // Pass false to u_Clicked(Draw cube with specified color)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix)
    pixels(0) === 255

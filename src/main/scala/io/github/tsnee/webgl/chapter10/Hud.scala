package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Element => _, Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object Hud extends Exercise:
  override val label: String = "HUD"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  val vertexShaderSource: String =
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

  val fragmentShaderSource: String =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  override def build: Element =
    val component = document.createElement("div")
    component.setAttribute("id", Exercise.componentId)
    (document.createElement("canvas"), document.createElement("canvas")) match
      case (canvasWebgl: Canvas, canvasHud: Canvas) =>
        canvasWebgl.height = Exercise.canvasHeight
        canvasHud.height = Exercise.canvasHeight
        canvasWebgl.width = Exercise.canvasWidth
        canvasHud.width = Exercise.canvasWidth
        canvasWebgl.setAttribute("style", "position: absolute; z-index: 0;")
        canvasHud.setAttribute("style", "position: absolute; z-index: 1;")
        initialize2(canvasWebgl, canvasHud)
        component.appendChild(canvasWebgl)
        component.appendChild(canvasHud)
        component

  def initialize(@unused canvas: Canvas): Unit = ()

  private def initialize2(canvasWebgl: Canvas, canvasHud: Canvas): Unit =
    Option(canvasHud.getContext("2d")) match
      case Some(ctx: CanvasRenderingContext2D) =>
        WebglInitializer.initialize(
          canvasWebgl,
          vertexShaderSource,
          fragmentShaderSource,
          run(canvasHud, ctx, _, _)
        )
      case _                                   =>
        val err = document.createElement("h1")
        err.innerText = "Failed to get CanvasRenderingContext2D from canvas element"
        canvasHud.replaceWith(err)

  private def run(
      canvasHud: Canvas,
      ctx: CanvasRenderingContext2D,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val numIndices     = setupBuffers(gl, program)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val projMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 3f, eyeY = 3f, eyeZ = 7f, atX = 0f, atY = 0f, atZ = 0f, upX = 0f, upY = 1f, upZ = 0f)
    val viewProjMatrix = projMatrix * viewMatrix
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val uClicked       = gl.getUniformLocation(program, "u_Clicked")
    gl.uniform1i(uClicked, 0)
    registerMouseListeners(canvasHud, gl, numIndices, uClicked, uMvpMatrix, viewProjMatrix, curAngle = 0f)
    val curTs          = js.Date.now()
    tick(gl, ctx, numIndices, uMvpMatrix, viewProjMatrix, curAngle = 0f, curTs)(curTs)

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
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    val colors      = Float32Array(js.Array[Float](
      0.2, 0.58, 0.82, 0.2, 0.58, 0.82, 0.2, 0.58, 0.82, 0.2, 0.58, 0.82,     // v0-v1-v2-v3 front
      0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69, 0.5, 0.41, 0.69,     // v0-v3-v4-v5 right
      0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61, 0.0, 0.32, 0.61,     // v0-v5-v6-v1 up
      0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, 0.78, 0.69, 0.84, // v1-v6-v7-v2 left
      0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, 0.32, 0.18, 0.56, // v7-v4-v3-v2 down
      0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93, 0.73, 0.82, 0.93  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, colors, 3, WebGLRenderingContext.FLOAT, "a_Color")
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

  private def registerMouseListeners(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      numIndices: Int,
      uClicked: WebGLUniformLocation,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      curAngle: Float
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
            check(gl, numIndices, uClicked, xInCanvas.toInt, yInCanvas.toInt, uMvpMatrix, viewProjMatrix, curAngle)
          if picked then window.alert("The cube was selected!")
    )

  private def tick(
      gl: WebGLRenderingContext,
      ctx: CanvasRenderingContext2D,
      numIndices: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      curAngle: Float,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    val nextAngle = animate(curAngle, prevTs, curTs)
    draw2d(ctx, nextAngle)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix, nextAngle)
    val _         = window.requestAnimationFrame(tick(gl, ctx, numIndices, uMvpMatrix, viewProjMatrix, nextAngle, curTs)(_))

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

  private def draw2d(ctx: CanvasRenderingContext2D, curAngle: Float): Unit =
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
    ctx.fillText("Current Angle: " + Math.floor(curAngle), 40, 240)

  private def check(
      gl: WebGLRenderingContext,
      numIndices: Int,
      uClicked: WebGLUniformLocation,
      x: Int,
      y: Int,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      curAngle: Float
  ): Boolean =
    gl.uniform1i(uClicked, 1); // Pass true to u_Clicked(Draw cube with red)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix, curAngle);
    // Read pixel at the clicked position
    val pixels = Uint8Array(4); // Array for storing the pixel value
    gl.readPixels(x, y, 1, 1, WebGLRenderingContext.RGBA, WebGLRenderingContext.UNSIGNED_BYTE, pixels);
    gl.uniform1i(uClicked, 0); // Pass false to u_Clicked(Draw cube with specified color)
    draw(gl, numIndices, uMvpMatrix, viewProjMatrix, curAngle)
    pixels(0) === 255

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

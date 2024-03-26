package io.github.tsnee.webgl.chapter9

import com.raquo.laminar.api.L.{Element => _, Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object JointModel extends Exercise:
  override val label: String = "JointModel"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  override def height: Int = 525

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Normal;
uniform mat4 u_MvpMatrix;
uniform mat4 u_NormalMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  vec3 lightDirection = normalize(vec3(0.0, 0.5, 0.7));
  vec4 color = vec4(1.0, 0.4, 0.0, 1.0);
  vec3 normal = normalize((u_NormalMatrix * a_Normal).xyz);
  float nDotL = max(dot(normal, lightDirection), 0.0);
  v_Color = vec4(color.rgb * nDotL + vec3(0.1), color.a);
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

  private val angleStepDegrees   = 3f
  private val gAngles            = Array.ofDim[Float](2)
  private val arm1AngleDegrees   = 0 // index into gAngles
  private val joint1AngleDegrees = 1 // index into gAngles

  def initialize(canvas: Canvas): Unit =
    Array(-90f, 0f).copyToArray(gAngles)
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
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val uMvpMatrix    = gl.getUniformLocation(program, "u_MvpMatrix")
    val uNormalMatrix = gl.getUniformLocation(program, "u_NormalMatrix")
    val projMatrix    = Matrix4.setPerspective(50f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix    =
      Matrix4.setLookAt(eyeX = 20f, eyeY = 10f, eyeZ = 30f, atX = 0f, atY = 0f, atZ = 0f, upX = 0f, upY = 1f, upZ = 0f)
    val n             = initVertexBuffers(gl, program)
    canvas.setAttribute("tabindex", "0") // required for canvas to be able to receive focus & therefore KeyboardEvents
    canvas.addEventListener("keydown", keyDown(gl, n, projMatrix * viewMatrix, uMvpMatrix, uNormalMatrix)(_))
    draw(gl, n, projMatrix * viewMatrix, uMvpMatrix, uNormalMatrix)

  private def keyDown(
      gl: WebGLRenderingContext,
      n: Int,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation,
      uNormalMatrix: WebGLUniformLocation
  )(
      ev: KeyboardEvent
  ): Unit =
    ev.preventDefault()
    val joint1AngleDelta =
      ev.key match
        case KeyValue.ArrowUp if gAngles(joint1AngleDegrees) < 135f    => +angleStepDegrees
        case KeyValue.ArrowDown if gAngles(joint1AngleDegrees) > -135f => -angleStepDegrees
        case _                                                         => 0f
    gAngles(joint1AngleDegrees) += joint1AngleDelta
    val arm1AngleDelta   =
      ev.key match
        case KeyValue.ArrowRight => +angleStepDegrees
        case KeyValue.ArrowLeft  => -angleStepDegrees
        case _                   => 0f
    gAngles(arm1AngleDegrees) += arm1AngleDelta
    draw(gl, n, viewProjMatrix, uMvpMatrix, uNormalMatrix)

  private def initVertexBuffers(gl: WebGLRenderingContext, program: WebGLProgram): Int =
    // Vertex coordinates（a cuboid 3.0 in width, 10.0 in height, and 3.0 in length with its origin at the center of its
    // bottom)
    val vertices    = new Float32Array(js.Array(
      1.5f, 10.0f, 1.5f, -1.5f, 10.0f, 1.5f, -1.5f, 0.0f, 1.5f, 1.5f, 0.0f, 1.5f,     // v0-v1-v2-v3 front
      1.5f, 10.0f, 1.5f, 1.5f, 0.0f, 1.5f, 1.5f, 0.0f, -1.5f, 1.5f, 10.0f, -1.5f,     // v0-v3-v4-v5 right
      1.5f, 10.0f, 1.5f, 1.5f, 10.0f, -1.5f, -1.5f, 10.0f, -1.5f, -1.5f, 10.0f, 1.5f, // v0-v5-v6-v1 up
      -1.5f, 10.0f, 1.5f, -1.5f, 10.0f, -1.5f, -1.5f, 0.0f, -1.5f, -1.5f, 0.0f, 1.5f, // v1-v6-v7-v2 left
      -1.5f, 0.0f, -1.5f, 1.5f, 0.0f, -1.5f, 1.5f, 0.0f, 1.5f, -1.5f, 0.0f, 1.5f,     // v7-v4-v3-v2 down
      1.5f, 0.0f, -1.5f, -1.5f, 0.0f, -1.5f, -1.5f, 10.0f, -1.5f, 1.5f, 10.0f, -1.5f  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    // Normal
    val normals     = new Float32Array(js.Array(
      0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,     // v0-v1-v2-v3 front
      1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,     // v0-v3-v4-v5 right
      0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,     // v0-v5-v6-v1 up
      -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, // v1-v6-v7-v2 left
      0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, // v7-v4-v3-v2 down
      0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, normals, 3, WebGLRenderingContext.FLOAT, "a_Normal")
    // Indices of the vertices
    val indices     = new Uint8Array(js.Array[Short](
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

  private def draw(
      gl: WebGLRenderingContext,
      n: Int,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation,
      uNormalMatrix: WebGLUniformLocation
  ): Unit =
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    // Arm1
    val arm1Length      = 10f
    val arm1ModelMatrix = Matrix4
      .setTranslate(0f, -12f, 0f)
      .rotate(gAngles(arm1AngleDegrees), 0f, 1f, 0f)
    drawBox(gl, n, viewProjMatrix, uMvpMatrix, uNormalMatrix, arm1ModelMatrix)
    // Arm2
    val arm2ModelMatrix = arm1ModelMatrix // this is what makes arm1 higher in the hierarchy than arm2
      .translate(0f, arm1Length, 0f)
      .rotate(gAngles(joint1AngleDegrees), 0f, 0f, 1f)
      .scale(1.3, 1, 1.3)
    drawBox(gl, n, viewProjMatrix, uMvpMatrix, uNormalMatrix, arm2ModelMatrix)

  private def drawBox(
      gl: WebGLRenderingContext,
      n: Int,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation,
      uNormalMatrix: WebGLUniformLocation,
      modelMatrix: Matrix4
  ): Unit =
    val mvpMatrix    = viewProjMatrix * modelMatrix
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    val normalMatrix = modelMatrix.invert.transpose
    gl.uniformMatrix4fv(uNormalMatrix, transpose = false, normalMatrix.toFloat32Array)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = n,
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

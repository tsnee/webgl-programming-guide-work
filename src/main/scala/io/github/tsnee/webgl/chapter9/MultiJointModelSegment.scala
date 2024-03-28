package io.github.tsnee.webgl.chapter9

import cats.syntax.all._
import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExerciseBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.scalajs.js
import scala.scalajs.js.typedarray._

object MultiJointModelSegment:
  val vertexShaderSource: VertexShaderSource =
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

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  private val angleStepDegrees   = 3f
  private val arm1AngleDegrees   = Var[Float](90)
  private val joint1AngleDegrees = Var[Float](45)
  private val joint2AngleDegrees = Var[Float](0)
  private val joint3AngleDegrees = Var[Float](0)

  def panel(height: Height, width: Width): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    gl.clearColor(0, 0, 0, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val uNormalMatrix  = gl.getUniformLocation(program, "u_NormalMatrix")
    val projMatrix     = Matrix4.setPerspective(50, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 20, eyeY = 10, eyeZ = 30, atX = 0, atY = 0, atZ = 0, upX = 0, upY = 1, upZ = 0)
    // Vertex coordinate (prepare coordinates of cuboids for all segments)
    val verticesBase   = Float32Array(js.Array[Float]( // Base(10x2x10)
      5.0, 2.0, 5.0, -5.0, 2.0, 5.0, -5.0, 0.0, 5.0, 5.0, 0.0, 5.0,     // v0-v1-v2-v3 front
      5.0, 2.0, 5.0, 5.0, 0.0, 5.0, 5.0, 0.0, -5.0, 5.0, 2.0, -5.0,     // v0-v3-v4-v5 right
      5.0, 2.0, 5.0, 5.0, 2.0, -5.0, -5.0, 2.0, -5.0, -5.0, 2.0, 5.0,   // v0-v5-v6-v1 up
      -5.0, 2.0, 5.0, -5.0, 2.0, -5.0, -5.0, 0.0, -5.0, -5.0, 0.0, 5.0, // v1-v6-v7-v2 left
      -5.0, 0.0, -5.0, 5.0, 0.0, -5.0, 5.0, 0.0, 5.0, -5.0, 0.0, 5.0,   // v7-v4-v3-v2 down
      5.0, 0.0, -5.0, -5.0, 0.0, -5.0, -5.0, 2.0, -5.0, 5.0, 2.0, -5.0  // v4-v7-v6-v5 back
    ))
    val baseBuffer     = VertexBufferObject.initializeVbo(gl, verticesBase)
    val verticesArm1   = Float32Array(js.Array[Float]( // Arm1(3x10x3)
      1.5, 10.0, 1.5, -1.5, 10.0, 1.5, -1.5, 0.0, 1.5, 1.5, 0.0, 1.5,     // v0-v1-v2-v3 front
      1.5, 10.0, 1.5, 1.5, 0.0, 1.5, 1.5, 0.0, -1.5, 1.5, 10.0, -1.5,     // v0-v3-v4-v5 right
      1.5, 10.0, 1.5, 1.5, 10.0, -1.5, -1.5, 10.0, -1.5, -1.5, 10.0, 1.5, // v0-v5-v6-v1 up
      -1.5, 10.0, 1.5, -1.5, 10.0, -1.5, -1.5, 0.0, -1.5, -1.5, 0.0, 1.5, // v1-v6-v7-v2 left
      -1.5, 0.0, -1.5, 1.5, 0.0, -1.5, 1.5, 0.0, 1.5, -1.5, 0.0, 1.5,     // v7-v4-v3-v2 down
      1.5, 0.0, -1.5, -1.5, 0.0, -1.5, -1.5, 10.0, -1.5, 1.5, 10.0, -1.5  // v4-v7-v6-v5 back
    ))
    val arm1Buffer     = VertexBufferObject.initializeVbo(gl, verticesArm1)
    val verticesArm2   = Float32Array(js.Array[Float]( // Arm2(4x10x4)
      2.0, 10.0, 2.0, -2.0, 10.0, 2.0, -2.0, 0.0, 2.0, 2.0, 0.0, 2.0,     // v0-v1-v2-v3 front
      2.0, 10.0, 2.0, 2.0, 0.0, 2.0, 2.0, 0.0, -2.0, 2.0, 10.0, -2.0,     // v0-v3-v4-v5 right
      2.0, 10.0, 2.0, 2.0, 10.0, -2.0, -2.0, 10.0, -2.0, -2.0, 10.0, 2.0, // v0-v5-v6-v1 up
      -2.0, 10.0, 2.0, -2.0, 10.0, -2.0, -2.0, 0.0, -2.0, -2.0, 0.0, 2.0, // v1-v6-v7-v2 left
      -2.0, 0.0, -2.0, 2.0, 0.0, -2.0, 2.0, 0.0, 2.0, -2.0, 0.0, 2.0,     // v7-v4-v3-v2 down
      2.0, 0.0, -2.0, -2.0, 0.0, -2.0, -2.0, 10.0, -2.0, 2.0, 10.0, -2.0  // v4-v7-v6-v5 back
    ))
    val arm2Buffer     = VertexBufferObject.initializeVbo(gl, verticesArm2)
    val verticesPalm   = Float32Array(js.Array[Float]( // Palm(2x2x6)
      1.0, 2.0, 3.0, -1.0, 2.0, 3.0, -1.0, 0.0, 3.0, 1.0, 0.0, 3.0,     // v0-v1-v2-v3 front
      1.0, 2.0, 3.0, 1.0, 0.0, 3.0, 1.0, 0.0, -3.0, 1.0, 2.0, -3.0,     // v0-v3-v4-v5 right
      1.0, 2.0, 3.0, 1.0, 2.0, -3.0, -1.0, 2.0, -3.0, -1.0, 2.0, 3.0,   // v0-v5-v6-v1 up
      -1.0, 2.0, 3.0, -1.0, 2.0, -3.0, -1.0, 0.0, -3.0, -1.0, 0.0, 3.0, // v1-v6-v7-v2 left
      -1.0, 0.0, -3.0, 1.0, 0.0, -3.0, 1.0, 0.0, 3.0, -1.0, 0.0, 3.0,   // v7-v4-v3-v2 down
      1.0, 0.0, -3.0, -1.0, 0.0, -3.0, -1.0, 2.0, -3.0, 1.0, 2.0, -3.0  // v4-v7-v6-v5 back
    ))
    val palmBuffer     = VertexBufferObject.initializeVbo(gl, verticesPalm)
    val verticesFinger = Float32Array(js.Array[Float]( // Fingers(1x2x1)
      0.5, 2.0, 0.5, -0.5, 2.0, 0.5, -0.5, 0.0, 0.5, 0.5, 0.0, 0.5,     // v0-v1-v2-v3 front
      0.5, 2.0, 0.5, 0.5, 0.0, 0.5, 0.5, 0.0, -0.5, 0.5, 2.0, -0.5,     // v0-v3-v4-v5 right
      0.5, 2.0, 0.5, 0.5, 2.0, -0.5, -0.5, 2.0, -0.5, -0.5, 2.0, 0.5,   // v0-v5-v6-v1 up
      -0.5, 2.0, 0.5, -0.5, 2.0, -0.5, -0.5, 0.0, -0.5, -0.5, 0.0, 0.5, // v1-v6-v7-v2 left
      -0.5, 0.0, -0.5, 0.5, 0.0, -0.5, 0.5, 0.0, 0.5, -0.5, 0.0, 0.5,   // v7-v4-v3-v2 down
      0.5, 0.0, -0.5, -0.5, 0.0, -0.5, -0.5, 2.0, -0.5, 0.5, 2.0, -0.5  // v4-v7-v6-v5 back
    ))
    val fingerBuffer   = VertexBufferObject.initializeVbo(gl, verticesFinger)
    // Normal
    val normals        = Float32Array(js.Array[Float](
      0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0,     // v0-v1-v2-v3 front
      1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0,     // v0-v3-v4-v5 right
      0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0,     // v0-v5-v6-v1 up
      -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, // v1-v6-v7-v2 left
      0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, // v7-v4-v3-v2 down
      0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0, 0.0, 0.0, -1.0  // v4-v7-v6-v5 back
    ))
    VertexBufferObject.initializeVbo(gl, normals)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Normal",
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
    val aPosition      = gl.getAttribLocation(program, "a_Position")
    canvas.amend(
      tabIndex := 0,
      onKeyDown --> keyDown(
        gl,
        indices.length,
        projMatrix * viewMatrix,
        aPosition,
        uMvpMatrix,
        uNormalMatrix,
        baseBuffer,
        arm1Buffer,
        arm2Buffer,
        palmBuffer,
        fingerBuffer
      )
    )
    draw(
      gl,
      indices.length,
      projMatrix * viewMatrix,
      aPosition,
      uMvpMatrix,
      uNormalMatrix,
      baseBuffer,
      arm1Buffer,
      arm2Buffer,
      palmBuffer,
      fingerBuffer
    )

  private def keyDown(
      gl: WebGLRenderingContext,
      n: Int,
      viewProjMatrix: Matrix4,
      aPosition: Int,
      uMvpMatrix: WebGLUniformLocation,
      uNormalMatrix: WebGLUniformLocation,
      baseBuffer: WebGLBuffer,
      arm1Buffer: WebGLBuffer,
      arm2Buffer: WebGLBuffer,
      palmBuffer: WebGLBuffer,
      fingerBuffer: WebGLBuffer
  )(
      ev: KeyboardEvent
  ): Unit =
    ev.preventDefault()
    val joint1AngleDelta =
      ev.key match
        case KeyValue.ArrowDown if joint1AngleDegrees.now() < 135f => +angleStepDegrees
        case KeyValue.ArrowUp if joint1AngleDegrees.now() > -135f  => -angleStepDegrees
        case _                                                     => 0f
    joint1AngleDegrees.update(_ + joint1AngleDelta)
    val arm1AngleDelta   =
      ev.key match
        case KeyValue.ArrowRight => +angleStepDegrees
        case KeyValue.ArrowLeft  => -angleStepDegrees
        case _                   => 0f
    arm1AngleDegrees.update(_ + arm1AngleDelta)
    val joint2AngleDelta =
      ev.key match
        case "z" => +angleStepDegrees
        case "x" => -angleStepDegrees
        case _   => 0f
    joint2AngleDegrees.update(_ + joint2AngleDelta)
    val joint3AngleDelta =
      ev.key match
        case "v" if joint3AngleDegrees.now() < 60  => +angleStepDegrees
        case "c" if joint3AngleDegrees.now() > -60 => -angleStepDegrees
        case _                                     => 0f
    joint3AngleDegrees.update(_ + joint3AngleDelta)
    if (arm1AngleDelta =!= 0f || joint1AngleDelta =!= 0f || joint2AngleDelta =!= 0f || joint3AngleDelta =!= 0f)
      draw(
        gl,
        n,
        viewProjMatrix,
        aPosition,
        uMvpMatrix,
        uNormalMatrix,
        baseBuffer,
        arm1Buffer,
        arm2Buffer,
        palmBuffer,
        fingerBuffer
      )

  private def draw(
      gl: WebGLRenderingContext,
      n: Int,
      viewProjMatrix: Matrix4,
      aPosition: Int,
      uMvpMatrix: WebGLUniformLocation,
      uNormalMatrix: WebGLUniformLocation,
      baseBuffer: WebGLBuffer,
      arm1Buffer: WebGLBuffer,
      arm2Buffer: WebGLBuffer,
      palmBuffer: WebGLBuffer,
      fingerBuffer: WebGLBuffer
  ): Unit =
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    // Base of assembly
    val baseHeight            = 2f
    val baseModelMatrix       = Matrix4.setTranslate(0, -12, 0)
    drawSegment(gl, n, baseBuffer, viewProjMatrix, baseModelMatrix, aPosition, uMvpMatrix, uNormalMatrix)
    // Arm1
    val arm1Length            = 10f
    val arm1ModelMatrix       = baseModelMatrix
      .translate(0f, baseHeight, 0f)
      .rotate(arm1AngleDegrees.now(), 0, 1, 0)
    drawSegment(gl, n, arm1Buffer, viewProjMatrix, arm1ModelMatrix, aPosition, uMvpMatrix, uNormalMatrix)
    // Arm2
    val arm2Length            = 10f
    val arm2ModelMatrix       = arm1ModelMatrix // this is what makes arm1 higher in the hierarchy than arm2
      .translate(0f, arm1Length, 0f)
      .rotate(joint1AngleDegrees.now(), 0, 0, 1)
    drawSegment(gl, n, arm2Buffer, viewProjMatrix, arm2ModelMatrix, aPosition, uMvpMatrix, uNormalMatrix)
    // Palm
    val palmLength            = 2f
    val palmModelMatrix       = arm2ModelMatrix.translate(0f, arm2Length, 0f).rotate(joint2AngleDegrees.now(), 0, 1, 0)
    drawSegment(gl, n, palmBuffer, viewProjMatrix, palmModelMatrix, aPosition, uMvpMatrix, uNormalMatrix)
    val baseFingerModelMatrix = palmModelMatrix.translate(0, palmLength, 0)

    val finger1ModelMatrix = baseFingerModelMatrix.translate(0, 0, 2).rotate(joint3AngleDegrees.now(), 1, 0, 0)
    drawSegment(gl, n, fingerBuffer, viewProjMatrix, finger1ModelMatrix, aPosition, uMvpMatrix, uNormalMatrix)
    val finger2ModelMatrix = baseFingerModelMatrix.translate(0, 0, -2).rotate(-joint3AngleDegrees.now(), 1, 0, 0)
    drawSegment(gl, n, fingerBuffer, viewProjMatrix, finger2ModelMatrix, aPosition, uMvpMatrix, uNormalMatrix)

  private def drawSegment(
      gl: WebGLRenderingContext,
      n: Int,
      buffer: WebGLBuffer,
      viewProjMatrix: Matrix4,
      modelMatrix: Matrix4,
      aPosition: Int,
      uMvpMatrix: WebGLUniformLocation,
      uNormalMatrix: WebGLUniformLocation
  ): Unit =
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, buffer)
    gl.vertexAttribPointer(aPosition, 3, WebGLRenderingContext.FLOAT, false, 0, 0)
    gl.enableVertexAttribArray(aPosition)
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

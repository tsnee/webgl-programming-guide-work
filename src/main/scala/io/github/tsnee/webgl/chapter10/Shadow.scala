// Derived from Shadow.js (c) 2012 matsuda and tanaka

package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ContextExtractor
import io.github.tsnee.webgl.common.ProgramCreator
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object Shadow:
  final private case class ShapeBuffers(
      vertexBufferWrapper: ArrayBufferWrapper,
      colorBufferWrapper: ArrayBufferWrapper,
      indexBuffer: WebGLBuffer,
      numIndices: Int
  )

  final private case class ProgramWrapper(
      program: WebGLProgram,
      aPosition: Int,
      aColor: Option[Int],
      uMvpMatrix: WebGLUniformLocation,
      uMvpMatrixFromLight: WebGLUniformLocation,
      uShadowMap: WebGLUniformLocation
  )

  private val shadowVertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
uniform mat4 u_MvpMatrix;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
}
"""

  private val shadowFragmentShaderSource: FragmentShaderSource = """
precision mediump float;
void main() {
  gl_FragColor = vec4(gl_FragCoord.z, 0.0, 0.0, 0.0);
}
"""

  private val vertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_MvpMatrix;
uniform mat4 u_MvpMatrixFromLight;
varying vec4 v_PositionFromLight;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  v_PositionFromLight = u_MvpMatrixFromLight * a_Position;
  v_Color = a_Color;
}
"""

  private val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
uniform sampler2D u_ShadowMap;
varying vec4 v_PositionFromLight;
varying vec4 v_Color;
void main() {
  vec3 shadowCoord =(v_PositionFromLight.xyz/v_PositionFromLight.w) / 2.0 + 0.5;
  vec4 rgbaDepth = texture2D(u_ShadowMap, shadowCoord.xy);
  float depth = rgbaDepth.r;
  float visibility = (shadowCoord.z > depth + 0.005) ? 0.7 : 1.0;
  gl_FragColor = vec4(v_Color.rgb * visibility, v_Color.a);
}
"""

  private val OffscreenWidth: Width   = 2048
  private val OffscreenHeight: Height = 2048
  private val LightX: Float           = 0
  private val LightY: Float           = 7
  private val LightZ: Float           = 2

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    val webglCanvas      = canvasTag(widthAttr := canvasWidth, heightAttr := canvasHeight)
    val successOrFailure =
      for
        gl            <- ContextExtractor.extractWebglContext(webglCanvas.ref)
        shadowProgram <- ProgramCreator.createProgram(gl, shadowVertexShaderSource, shadowFragmentShaderSource)
        normalProgram <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
        _              = useWebgl(webglCanvas, gl, shadowProgram, normalProgram)
      yield ()
    successOrFailure match
      case Right(())   => div(webglCanvas)
      case Left(error) => div(error)

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      shadowProgram: WebGLProgram,
      normalProgram: WebGLProgram
  ): Unit =
    val triangle    = initVertexBuffersForTriangle(gl)
    val plane       = initVertexBuffersForPlane(gl)
    val framebuffer = initFramebufferObject(gl)
    gl.activeTexture(WebGLRenderingContext.TEXTURE0)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, framebuffer.texture)

    gl.clearColor(0, 0, 0, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)

    val viewProjMatrixFromLight = Matrix4.setPerspective(70, OffscreenWidth.toFloat / OffscreenHeight, 1, 100)
      .lookAt(LightX, LightY, LightZ, 0, 0, 0, 0, 1, 0)
    val viewProjMatrix          = Matrix4.setPerspective(45, canvas.ref.width.toFloat / canvas.ref.height, 1, 100)
      .lookAt(0, 7, 9, 0, 0, 0, 0, 1, 0)

    val shadowProgramWrapper = ProgramWrapper(
      shadowProgram,
      gl.getAttribLocation(shadowProgram, "a_Position"),
      None,
      gl.getUniformLocation(shadowProgram, "u_MvpMatrix"),
      gl.getUniformLocation(shadowProgram, "u_MvpMatrixFromLight"),
      gl.getUniformLocation(shadowProgram, "u_ShadowMap")
    )
    val normalProgramWrapper = ProgramWrapper(
      normalProgram,
      gl.getAttribLocation(normalProgram, "a_Position"),
      gl.getAttribLocation(normalProgram, "a_Color").some,
      gl.getUniformLocation(normalProgram, "u_MvpMatrix"),
      gl.getUniformLocation(normalProgram, "u_MvpMatrixFromLight"),
      gl.getUniformLocation(normalProgram, "u_ShadowMap")
    )

    val now = js.Date.now()
    val _   = tick(
      canvas,
      gl,
      shadowProgramWrapper,
      normalProgramWrapper,
      framebuffer,
      triangle,
      plane,
      viewProjMatrix,
      viewProjMatrixFromLight,
      currentAngle = 0,
      prevTs = now
    )(now)

  private def tick(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      shadowProgram: ProgramWrapper,
      normalProgram: ProgramWrapper,
      framebuffer: FramebufferWrapper,
      triangle: ShapeBuffers,
      plane: ShapeBuffers,
      viewProjMatrix: Matrix4,
      viewProjMatrixFromLight: Matrix4,
      currentAngle: Float,
      prevTs: Double
  )(curTs: Double): Int =
    val nextAngle = animate(currentAngle, prevTs, curTs)
    gl.bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, framebuffer.framebuffer)
    gl.viewport(0, 0, OffscreenWidth, OffscreenHeight)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)

    gl.useProgram(shadowProgram.program)
    val mvpMatrixFromLightTriangle = drawTriangle(gl, shadowProgram, triangle, viewProjMatrixFromLight, nextAngle)
    val mvpMatrixFromLightPlane    = drawPlane(gl, shadowProgram, plane, viewProjMatrixFromLight)

    gl.bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, None.orNull)
    gl.viewport(0, 0, canvas.ref.width, canvas.ref.height)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)

    gl.useProgram(normalProgram.program)
    gl.uniform1i(normalProgram.uShadowMap, 0)
    gl.uniformMatrix4fv(normalProgram.uMvpMatrixFromLight, transpose = false, mvpMatrixFromLightTriangle.toFloat32Array)
    drawTriangle(gl, normalProgram, triangle, viewProjMatrix, nextAngle)
    gl.uniformMatrix4fv(normalProgram.uMvpMatrixFromLight, transpose = false, mvpMatrixFromLightPlane.toFloat32Array)
    drawPlane(gl, normalProgram, plane, viewProjMatrix)
    window.requestAnimationFrame(tick(
      canvas,
      gl,
      shadowProgram,
      normalProgram,
      framebuffer,
      triangle,
      plane,
      viewProjMatrix,
      viewProjMatrixFromLight,
      nextAngle,
      curTs
    )(_))

  private def drawTriangle(
      gl: WebGLRenderingContext,
      programWrapper: ProgramWrapper,
      shapeBuffers: ShapeBuffers,
      viewProjMatrix: Matrix4,
      currentAngle: Float
  ): Matrix4 =
    val modelMatrix = Matrix4.setRotate(currentAngle, 0, 1, 0)
    draw(
      gl,
      programWrapper,
      modelMatrix,
      shapeBuffers,
      viewProjMatrix
    )

  private def drawPlane(
      gl: WebGLRenderingContext,
      programWrapper: ProgramWrapper,
      shapeBuffers: ShapeBuffers,
      viewProjMatrix: Matrix4
  ): Matrix4 =
    val modelMatrix = Matrix4.setRotate(-45, 0, 1, 1)
    draw(
      gl,
      programWrapper,
      modelMatrix,
      shapeBuffers,
      viewProjMatrix
    )

  private def draw(
      gl: WebGLRenderingContext,
      programWrapper: ProgramWrapper,
      modelMatrix: Matrix4,
      shapeBuffers: ShapeBuffers,
      viewProjMatrix: Matrix4
  ): Matrix4 =
    initAttributeVariable(gl, programWrapper.aPosition, shapeBuffers.vertexBufferWrapper)
    programWrapper.aColor.foreach(initAttributeVariable(gl, _, shapeBuffers.colorBufferWrapper))
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, shapeBuffers.indexBuffer)
    val mvpMatrix = viewProjMatrix * modelMatrix
    gl.uniformMatrix4fv(programWrapper.uMvpMatrix, transpose = false, mvpMatrix.toFloat32Array)
    gl.drawElements(WebGLRenderingContext.TRIANGLES, shapeBuffers.numIndices, WebGLRenderingContext.UNSIGNED_BYTE, 0)
    mvpMatrix

  private def initAttributeVariable(gl: WebGLRenderingContext, attr: Int, bufferWrapper: ArrayBufferWrapper): Unit =
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, bufferWrapper.buffer)
    gl.vertexAttribPointer(attr, bufferWrapper.size, bufferWrapper.typ, normalized = false, stride = 0, offset = 0)
    gl.enableVertexAttribArray(attr)

  private def initVertexBuffersForPlane(gl: WebGLRenderingContext): ShapeBuffers =
    // Create a plane
    //  v1------v0
    //  |        |
    //  |        |
    //  |        |
    //  v2------v3
    val vertices            = Float32Array(js.Array[Float](
      3.0, -1.7, 2.5, -3.0, -1.7, 2.5, -3.0, -1.7, -2.5, 3.0, -1.7, -2.5 // v0-v1-v2-v3
    ))
    val vertexBufferWrapper =
      ArrayBufferWrapper(VertexBufferObject.initializeVbo(gl, vertices), 3, WebGLRenderingContext.FLOAT)
    val colors              = Float32Array(js.Array[Float](
      1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0
    ))
    val colorBufferWrapper  =
      ArrayBufferWrapper(VertexBufferObject.initializeVbo(gl, colors), 3, WebGLRenderingContext.FLOAT)
    val indices             = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3
    ))
    val indexBuffer         = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, None.orNull)
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, None.orNull)
    ShapeBuffers(vertexBufferWrapper, colorBufferWrapper, indexBuffer, indices.length)

  private def initVertexBuffersForTriangle(gl: WebGLRenderingContext): ShapeBuffers =
    // Create a triangle
    //       v2
    //      / |
    //     /  |
    //    /   |
    //  v0----v1
    val vertices            = Float32Array(js.Array[Float](-0.8, 3.5, 0.0, 0.8, 3.5, 0.0, 0.0, 3.5, 1.8))
    val vertexBufferWrapper =
      ArrayBufferWrapper(VertexBufferObject.initializeVbo(gl, vertices), 3, WebGLRenderingContext.FLOAT)
    val colors              = Float32Array(js.Array[Float](1.0, 0.5, 0.0, 1.0, 0.5, 0.0, 1.0, 0.0, 0.0))
    val colorBufferWrapper  =
      ArrayBufferWrapper(VertexBufferObject.initializeVbo(gl, colors), 3, WebGLRenderingContext.FLOAT)
    val indices             = Uint8Array(js.Array[Short](0, 1, 2))
    val indexBuffer         = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, None.orNull)
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, None.orNull)
    ShapeBuffers(vertexBufferWrapper, colorBufferWrapper, indexBuffer, indices.length)

  private def initFramebufferObject(gl: WebGLRenderingContext): FramebufferWrapper =
    val fbo     = gl.createFramebuffer()
    val texture = gl.createTexture()
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture)
    gl.texImage2D(
      target = WebGLRenderingContext.TEXTURE_2D,
      level = 0,
      internalformat = WebGLRenderingContext.RGBA,
      width = OffscreenWidth,
      height = OffscreenHeight,
      border = 0,
      format = WebGLRenderingContext.RGBA,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      pixels = None.orNull
    )
    gl.texParameteri(
      WebGLRenderingContext.TEXTURE_2D,
      WebGLRenderingContext.TEXTURE_MIN_FILTER,
      WebGLRenderingContext.LINEAR
    )

    val depthBuffer = gl.createRenderbuffer()
    gl.bindRenderbuffer(WebGLRenderingContext.RENDERBUFFER, depthBuffer)
    gl.renderbufferStorage(
      WebGLRenderingContext.RENDERBUFFER,
      WebGLRenderingContext.DEPTH_COMPONENT16,
      OffscreenWidth,
      OffscreenHeight
    )
    gl.bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, fbo)
    gl.framebufferTexture2D(
      WebGLRenderingContext.FRAMEBUFFER,
      WebGLRenderingContext.COLOR_ATTACHMENT0,
      WebGLRenderingContext.TEXTURE_2D,
      texture,
      0
    )
    gl.framebufferRenderbuffer(
      WebGLRenderingContext.FRAMEBUFFER,
      WebGLRenderingContext.DEPTH_ATTACHMENT,
      WebGLRenderingContext.RENDERBUFFER,
      depthBuffer
    )
    val e           = gl.checkFramebufferStatus(WebGLRenderingContext.FRAMEBUFFER)
    if e =!= WebGLRenderingContext.FRAMEBUFFER_COMPLETE then
      console.log(s"Frame buffer object is incomplete: $e")
      gl.deleteFramebuffer(fbo)
      gl.deleteTexture(texture)
      gl.deleteRenderbuffer(depthBuffer)
    gl.bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, None.orNull)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, None.orNull)
    gl.bindRenderbuffer(WebGLRenderingContext.RENDERBUFFER, None.orNull)
    FramebufferWrapper(fbo, texture)

  private val AngleStepDegrees: Float = 40

  private def animate(currentAngle: Float, prevTs: Double, curTs: Double): Float =
    val elapsed = (curTs - prevTs).toFloat
    (currentAngle + AngleStepDegrees * elapsed / 1000) % 360

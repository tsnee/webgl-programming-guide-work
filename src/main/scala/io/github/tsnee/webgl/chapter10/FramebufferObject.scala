package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common._
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray._

final case class ArrayBufferWrapper(buffer: WebGLBuffer, size: Int, typ: Int)
final case class FramebufferWrapper(
    framebuffer: WebGLFramebuffer,
    texture: WebGLTexture
)

object FramebufferObject:
  final case class ShapeBuffers(
      vertexBufferWrapper: ArrayBufferWrapper,
      texCoordsBufferWrapper: ArrayBufferWrapper,
      indices: WebGLBuffer,
      numIndices: Int
  )

  private val vertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
attribute vec2 a_TexCoord;
uniform mat4 u_MvpMatrix;
varying vec2 v_TexCoord;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  v_TexCoord = a_TexCoord;
}
"""

  private val fragmentShaderSource: FragmentShaderSource = """
#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D u_Sampler;
varying vec2 v_TexCoord;
void main() {
  gl_FragColor = texture2D(u_Sampler, v_TexCoord);
}
"""

  private val OffscreenWidth: Int  = 256
  private val OffscreenHeight: Int = 256

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(canvasWidth, canvasHeight)

  private def useWebgl(
      canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    gl.useProgram(program)
    val aPosition          = gl.getAttribLocation(program, "a_Position")
    val aTexCoords         = gl.getAttribLocation(program, "a_TexCoord")
    val uMvpMatrix         = gl.getUniformLocation(program, "u_MvpMatrix")
    val cube               = initVertexBuffersForCube(gl)
    val plane              = initVertexBuffersForPlane(gl)
    val texture            = initTextures(gl, program)
    val framebufferWrapper = initFrameBufferObject(gl)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    val viewProjMatrix     = Matrix4.setPerspective(
      fov = 30,
      aspect = canvas.ref.width.toFloat / canvas.ref.height,
      near = 1,
      far = 100
    )
      .lookAt(
        0, 0, 7,
        0, 0, 0,
        0, 1, 0
      )
    val viewProjMatrixFbo  = Matrix4.setPerspective(
      fov = 30,
      aspect = OffscreenWidth.toFloat / OffscreenHeight,
      near = 1,
      far = 100
    )
      .lookAt(
        0, 2, 7,
        0, 0, 0,
        0, 1, 0
      )
    val ts                 = js.Date.now()
    tick(
      gl,
      program,
      canvas.ref.width,
      canvas.ref.height,
      texture,
      aPosition,
      aTexCoords,
      framebufferWrapper,
      plane,
      cube,
      viewProjMatrix,
      viewProjMatrixFbo,
      currentAngle = 0,
      uMvpMatrix = uMvpMatrix,
      prevTs = ts
    )(ts)

  private def tick(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      canvasWidth: Int,
      canvasHeight: Int,
      texture: WebGLTexture,
      aPosition: Int,
      aTexCoords: Int,
      framebufferWrapper: FramebufferWrapper,
      plane: ShapeBuffers,
      cube: ShapeBuffers,
      viewProjMatrix: Matrix4,
      viewProjMatrixFbo: Matrix4,
      currentAngle: Float,
      uMvpMatrix: WebGLUniformLocation,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    val nextAngle = animate(currentAngle, prevTs, curTs)
    draw(
      gl,
      canvasWidth,
      canvasHeight,
      framebufferWrapper,
      texture,
      aPosition,
      aTexCoords,
      cube,
      plane,
      nextAngle,
      viewProjMatrix,
      viewProjMatrixFbo,
      uMvpMatrix
    )
    val _         = window.requestAnimationFrame(tick(
      gl,
      program,
      canvasWidth,
      canvasHeight,
      texture,
      aPosition,
      aTexCoords,
      framebufferWrapper,
      plane,
      cube,
      viewProjMatrix,
      viewProjMatrixFbo,
      nextAngle,
      uMvpMatrix,
      curTs
    )(_))

  private def initVertexBuffersForCube(gl: WebGLRenderingContext): ShapeBuffers =
    //    v6----- v5
    //   /|      /|
    //  v1------v0|
    //  | |     | |
    //  | |v7---|-|v4
    //  |/      |/
    //  v2------v3
    val vertices            = new Float32Array(js.Array[Float](
      1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,     // v0-v1-v2-v3 front
      1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,     // v0-v3-v4-v5 right
      1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,     // v0-v5-v6-v1 up
      -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1, // v1-v6-v7-v2 left
      -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1, // v7-v4-v3-v2 down
      1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1  // v4-v7-v6-v5 back
    ))
    val vertexBuffer        = VertexBufferObject.initializeVbo(gl, vertices)
    val vertexBufferWrapper = ArrayBufferWrapper(vertexBuffer, 3, WebGLRenderingContext.FLOAT)

    val texCoords              = new Float32Array(js.Array[Float](
      1, 1, 0, 1, 0, 0, 1, 0, // v0-v1-v2-v3 front
      0, 1, 0, 0, 1, 0, 1, 1, // v0-v3-v4-v5 right
      1, 0, 1, 1, 0, 1, 0, 0, // v0-v5-v6-v1 up
      1, 1, 0, 1, 0, 0, 1, 0, // v1-v6-v7-v2 left
      0, 0, 1, 0, 1, 1, 0, 1, // v7-v4-v3-v2 down
      0, 0, 1, 0, 1, 1, 0, 1  // v4-v7-v6-v5 back
    ))
    val texCoordsBuffer        = VertexBufferObject.initializeVbo(gl, texCoords)
    val texCoordsBufferWrapper = ArrayBufferWrapper(texCoordsBuffer, 2, WebGLRenderingContext.FLOAT)

    val indices     = new Uint8Array(js.Array[Short]( // Indices of the vertices
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
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, None.orNull)
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, None.orNull)
    ShapeBuffers(vertexBufferWrapper, texCoordsBufferWrapper, indexBuffer, indices.length)

  private def initVertexBuffersForPlane(gl: WebGLRenderingContext): ShapeBuffers =
    // Create face
    //  v1------v0
    //  |        |
    //  |        |
    //  |        |
    //  v2------v3
    val vertices            = Float32Array(js.Array[Float](
      1.0, 1.0, 0.0, -1.0, 1.0, 0.0, -1.0, -1.0, 0.0, 1.0, -1.0, 0.0 // v0-v1-v2-v3
    ))
    val vbo                 = VertexBufferObject.initializeVbo(gl, vertices)
    val vertexBufferWrapper = ArrayBufferWrapper(vbo, 3, WebGLRenderingContext.FLOAT)

    val texCoords              = Float32Array(js.Array[Float](
      1.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0
    ))
    val texCoordsBuffer        = VertexBufferObject.initializeVbo(gl, texCoords)
    val texCoordsBufferWrapper = ArrayBufferWrapper(texCoordsBuffer, 2, WebGLRenderingContext.FLOAT)

    val indices     = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3
    ))
    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, None.orNull)
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, None.orNull)
    ShapeBuffers(vertexBufferWrapper, texCoordsBufferWrapper, indexBuffer, indices.length)

  private def initTextures(gl: WebGLRenderingContext, program: WebGLProgram): WebGLTexture =
    val texture  = gl.createTexture()
    val uSampler = gl.getUniformLocation(program, "u_Sampler")
    val jpeg     = Image()
    jpeg.addEventListener("load", imageLoaded(gl, texture, jpeg, uSampler)(_))
    jpeg.src = "sky_cloud.jpg"
    texture

  private def imageLoaded(
      gl: WebGLRenderingContext,
      texture: WebGLTexture,
      jpeg: Image,
      uSampler: WebGLUniformLocation
  )(
      @unused evt: Event
  ): Unit =
    gl.pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture)
    gl.texParameteri(
      WebGLRenderingContext.TEXTURE_2D,
      WebGLRenderingContext.TEXTURE_MIN_FILTER,
      WebGLRenderingContext.LINEAR
    )
    gl.texImage2D(
      WebGLRenderingContext.TEXTURE_2D,
      0,
      WebGLRenderingContext.RGBA,
      WebGLRenderingContext.RGBA,
      WebGLRenderingContext.UNSIGNED_BYTE,
      jpeg
    )
    gl.uniform1i(uSampler, 0)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, None.orNull)

  private def initFrameBufferObject(gl: WebGLRenderingContext): FramebufferWrapper =
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

  private def draw(
      gl: WebGLRenderingContext,
      canvasWidth: Int,
      canvasHeight: Int,
      framebufferWrapper: FramebufferWrapper,
      texture: WebGLTexture,
      aPosition: Int,
      aTexCoords: Int,
      cube: ShapeBuffers,
      plane: ShapeBuffers,
      currentAngle: Float,
      viewProjMatrix: Matrix4,
      viewProjMatrixFbo: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Unit =
    gl.bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, framebufferWrapper.framebuffer)
    gl.viewport(0, 0, OffscreenWidth, OffscreenHeight)
    gl.clearColor(0.2, 0.2, 0.4, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    drawTexturedCube(gl, texture, aPosition, aTexCoords, cube, currentAngle, viewProjMatrixFbo, uMvpMatrix)

    gl.bindFramebuffer(WebGLRenderingContext.FRAMEBUFFER, None.orNull)
    gl.viewport(0, 0, canvasWidth, canvasHeight)
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    drawTexturedPlane(
      gl,
      framebufferWrapper.texture,
      aPosition,
      aTexCoords,
      plane,
      currentAngle,
      viewProjMatrix,
      uMvpMatrix
    )

  private def drawTexturedCube(
      gl: WebGLRenderingContext,
      texture: WebGLTexture,
      aPosition: Int,
      aTexCoords: Int,
      shapeBuffers: ShapeBuffers,
      currentAngle: Float,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Unit =
    val modelMatrix = Matrix4.setRotate(20, 1, 0, 0)
      .rotate(currentAngle, 0, 1, 0)
    val mvpMatrix   = viewProjMatrix * modelMatrix
    gl.uniformMatrix4fv(uMvpMatrix, transpose = false, mvpMatrix.toFloat32Array)
    drawTexturedObject(gl, texture, aPosition, aTexCoords, shapeBuffers)

  private def drawTexturedPlane(
      gl: WebGLRenderingContext,
      texture: WebGLTexture,
      aPosition: Int,
      aTexCoords: Int,
      shapeBuffers: ShapeBuffers,
      currentAngle: Float,
      viewProjMatrix: Matrix4,
      uMvpMatrix: WebGLUniformLocation
  ): Unit =
    val modelMatrix = Matrix4.setTranslate(0, 0, 1)
      .rotate(20, 1, 0, 0)
      .rotate(currentAngle, 0, 1, 0)
    val mvpMatrix   = viewProjMatrix * modelMatrix
    gl.uniformMatrix4fv(uMvpMatrix, transpose = false, mvpMatrix.toFloat32Array)
    drawTexturedObject(gl, texture, aPosition, aTexCoords, shapeBuffers)

  private def drawTexturedObject(
      gl: WebGLRenderingContext,
      texture: WebGLTexture,
      aPosition: Int,
      aTexCoord: Int,
      shapeBuffers: ShapeBuffers
  ): Unit =
    initAttributeVariable(gl, aPosition, shapeBuffers.vertexBufferWrapper)
    initAttributeVariable(gl, aTexCoord, shapeBuffers.texCoordsBufferWrapper)
    gl.activeTexture(WebGLRenderingContext.TEXTURE0)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture)
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, shapeBuffers.indices)
    gl.drawElements(
      WebGLRenderingContext.TRIANGLES,
      shapeBuffers.numIndices,
      WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )

  private def initAttributeVariable(
      gl: WebGLRenderingContext,
      attr: Int,
      bufferWrapper: ArrayBufferWrapper
  ): Unit =
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, bufferWrapper.buffer)
    gl.vertexAttribPointer(
      indx = attr,
      size = bufferWrapper.size,
      `type` = bufferWrapper.typ,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(attr)

  private val angleStepDegrees = 30

  private def animate(currentAngle: Float, prevTs: Double, curTs: Double): Float =
    val elapsed = curTs - prevTs
    (currentAngle + (angleStepDegrees * elapsed).toFloat / 1000) % 360

package io.github.tsnee.webgl.chapter10

import com.raquo.laminar.api.L.{Image => _, _}
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common._
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.Event
import org.scalajs.dom.Image
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLTexture
import org.scalajs.dom.WebGLUniformLocation
import org.scalajs.dom.window

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray._

final case class ProgramParams(
    uNormalMatrix: WebGLUniformLocation,
    uMvpMatrix: WebGLUniformLocation
)

object ProgramObject:
  private val solidVertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
attribute vec4 a_Normal;
uniform mat4 u_MvpMatrix;
uniform mat4 u_NormalMatrix;
varying vec4 v_Color;
void main() {
  vec3 lightDirection = vec3(0.0, 0.0, 1.0);
  vec4 color = vec4(0.0, 1.0, 1.0, 1.0);     // Face color
  gl_Position = u_MvpMatrix * a_Position;
  vec3 normal = normalize(vec3(u_NormalMatrix * a_Normal));
  float nDotL = max(dot(normal, lightDirection), 0.0);
  v_Color = vec4(color.rgb * nDotL, color.a);
}
"""

  private val solidFragmentShaderSource: FragmentShaderSource = """
#ifdef GL_ES
precision mediump float;
#endif
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  private val textureVertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
attribute vec4 a_Normal;
attribute vec2 a_TexCoord;
uniform mat4 u_MvpMatrix;
uniform mat4 u_NormalMatrix;
varying float v_NdotL;
varying vec2 v_TexCoord;
void main() {
  vec3 lightDirection = vec3(0.0, 0.0, 1.0);
  gl_Position = u_MvpMatrix * a_Position;
  vec3 normal = normalize(vec3(u_NormalMatrix * a_Normal));
  v_NdotL = max(dot(normal, lightDirection), 0.0);
  v_TexCoord = a_TexCoord;
}
"""

  private val textureFragmentShaderSource: FragmentShaderSource = """
#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D u_Sampler;
varying vec2 v_TexCoord;
varying float v_NdotL;
void main() {
  vec4 color = texture2D(u_Sampler, v_TexCoord);
  gl_FragColor = vec4(color.rgb * v_NdotL, color.a);
}
"""

  private val vertices = new Float32Array(js.Array[Float](
    1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,     // v0-v1-v2-v3 front
    1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,     // v0-v3-v4-v5 right
    1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,     // v0-v5-v6-v1 up
    -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1, // v1-v6-v7-v2 left
    -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1, // v7-v4-v3-v2 down
    1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1  // v4-v7-v6-v5 back
  ))

  private val indices = new Uint8Array(js.Array[Short]( // Indices of the vertices
    0, 1, 2, 0, 2, 3,       // front
    4, 5, 6, 4, 6, 7,       // right
    8, 9, 10, 8, 10, 11,    // up
    12, 13, 14, 12, 14, 15, // left
    16, 17, 18, 16, 18, 19, // down
    20, 21, 22, 20, 22, 23  // back
  ))

  private val normals = new Float32Array(js.Array[Float](
    0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,     // v0-v1-v2-v3 front
    1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,     // v0-v3-v4-v5 right
    0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,     // v0-v5-v6-v1 up
    -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // v1-v6-v7-v2 left
    0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // v7-v4-v3-v2 down
    0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1  // v4-v7-v6-v5 back
  ))

  def panel(canvasHeight: Height, canvasWidth: Width): Element =
    val webglCanvas      = canvasTag(heightAttr := canvasHeight, widthAttr := canvasWidth)
    val successOrFailure =
      for
        gl             <- ContextExtractor.extractWebglContext(webglCanvas.ref)
        solidProgram   <- ProgramCreator.createProgram(gl, solidVertexShaderSource, solidFragmentShaderSource)
        textureProgram <- ProgramCreator.createProgram(gl, textureVertexShaderSource, textureFragmentShaderSource)
        _               = useWebgl(gl, solidProgram, textureProgram)
      yield ()
    successOrFailure match
      case Right(())   => div(webglCanvas)
      case Left(error) => div(error)

  private def useWebgl(
      gl: WebGLRenderingContext,
      solidProgram: WebGLProgram,
      textureProgram: WebGLProgram
  ): Unit =
    val solidProgramParams   = lookupProgramParams(gl, solidProgram)
    val textureProgramParams = lookupProgramParams(gl, textureProgram)
    initVertexBuffers(gl, solidProgram, textureProgram)
    val texture              = initTextures(gl, textureProgram)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.clearColor(0, 0, 0, 1)
    val viewProjMatrix       = Matrix4.setPerspective(
      fov = 30,
      aspect = gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight,
      near = 1,
      far = 100
    )
      .lookAt(
        0, 0, 15,
        0, 0, 0,
        0, 1, 0
      )
    val uSampler             = gl.getUniformLocation(textureProgram, "u_Sampler")
    gl.useProgram(textureProgram)
    gl.uniform1i(uSampler, 0)
    val ts                   = js.Date.now()
    tick(
      gl,
      solidProgram,
      solidProgramParams,
      textureProgram,
      textureProgramParams,
      texture,
      viewProjMatrix,
      currentAngle = 0,
      prevTs = ts
    )(ts)

  private def tick(
      gl: WebGLRenderingContext,
      solidProgram: WebGLProgram,
      solidProgramParams: ProgramParams,
      textureProgram: WebGLProgram,
      textureProgramParams: ProgramParams,
      texture: WebGLTexture,
      viewProjMatrix: Matrix4,
      currentAngle: Float,
      prevTs: Double
  )(
      curTs: Double
  ): Unit =
    val nextAngle = animate(currentAngle, prevTs, curTs)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    drawSolidCube(gl, solidProgram, solidProgramParams, -2, nextAngle, viewProjMatrix)
    drawTexCube(gl, textureProgram, textureProgramParams, texture, 2, nextAngle, viewProjMatrix)
    val _         = window.requestAnimationFrame(tick(
      gl,
      solidProgram,
      solidProgramParams,
      textureProgram,
      textureProgramParams,
      texture,
      viewProjMatrix,
      nextAngle,
      curTs
    )(_))

  private val angleStepDegrees = 30

  private def animate(currentAngle: Float, prevTs: Double, curTs: Double): Float =
    val elapsed = curTs - prevTs
    (currentAngle + (angleStepDegrees * elapsed).toFloat / 1000) % 360

  private def initVertexBuffers(
      gl: WebGLRenderingContext,
      solidProgram: WebGLProgram,
      textureProgram: WebGLProgram
  ): Unit =
    // Create a cube
    //    v6----- v5
    //   /|      /|
    //  v1------v0|
    //  | |     | |
    //  | |v7---|-|v4
    //  |/      |/
    //  v2------v3
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableAttribute(
      gl,
      solidProgram,
      WebGLRenderingContext.FLOAT,
      "a_Position",
      size = 3,
      stride = 0,
      offset = 0
    )
    WebglAttribute.enableAttribute(
      gl,
      textureProgram,
      WebGLRenderingContext.FLOAT,
      "a_Position",
      size = 3,
      stride = 0,
      offset = 0
    )

    VertexBufferObject.initializeVbo(gl, normals)
    WebglAttribute.enableAttribute(
      gl,
      solidProgram,
      WebGLRenderingContext.FLOAT,
      "a_Normal",
      size = 3,
      stride = 0,
      offset = 0
    )
    WebglAttribute.enableAttribute(
      gl,
      textureProgram,
      WebGLRenderingContext.FLOAT,
      "a_Normal",
      size = 3,
      stride = 0,
      offset = 0
    )

    val texCoords = new Float32Array(js.Array[Float](
      1, 1, 0, 1, 0, 0, 1, 0, // v0-v1-v2-v3 front
      0, 1, 0, 0, 1, 0, 1, 1, // v0-v3-v4-v5 right
      1, 0, 1, 1, 0, 1, 0, 0, // v0-v5-v6-v1 up
      1, 1, 0, 1, 0, 0, 1, 0, // v1-v6-v7-v2 left
      0, 0, 1, 0, 1, 1, 0, 1, // v7-v4-v3-v2 down
      0, 0, 1, 0, 1, 1, 0, 1  // v4-v7-v6-v5 back
    ))
    VertexBufferObject.initializeVbo(gl, texCoords)
    WebglAttribute.enableAttribute(
      gl,
      textureProgram,
      WebGLRenderingContext.FLOAT,
      "a_TexCoord",
      size = 2,
      stride = 0,
      offset = 0
    )

    val indexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)

  private def initTextures(gl: WebGLRenderingContext, textureProgram: WebGLProgram): WebGLTexture =
    val texture = gl.createTexture()
    val jpeg    = Image()
    jpeg.addEventListener("load", imageLoaded(gl, textureProgram, texture, jpeg)(_))
    jpeg.src = "orange.jpg"
    texture

  private def imageLoaded(
      gl: WebGLRenderingContext,
      textureProgram: WebGLProgram,
      texture: WebGLTexture,
      jpeg: Image
  )(@unused evt: Event): Unit =
    org.scalajs.dom.console.log(s"Image event listener")
    gl.pixelStorei(WebGLRenderingContext.UNPACK_FLIP_Y_WEBGL, 1)
    gl.activeTexture(WebGLRenderingContext.TEXTURE0)
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
    gl.useProgram(textureProgram)

  private def drawSolidCube(
      gl: WebGLRenderingContext,
      solidProgram: WebGLProgram,
      solidProgramParams: ProgramParams,
      leftOffset: Float,
      currentAngle: Float,
      viewProjMatrix: Matrix4
  ): Unit =
    gl.useProgram(solidProgram)
    drawCube(gl, solidProgramParams, leftOffset, currentAngle, viewProjMatrix)

  private def drawTexCube(
      gl: WebGLRenderingContext,
      textureProgram: WebGLProgram,
      textureProgramParams: ProgramParams,
      texture: WebGLTexture,
      leftOffset: Float,
      currentAngle: Float,
      viewProjMatrix: Matrix4
  ): Unit =
    gl.useProgram(textureProgram)
    gl.activeTexture(WebGLRenderingContext.TEXTURE0)
    gl.bindTexture(WebGLRenderingContext.TEXTURE_2D, texture)
    drawCube(gl, textureProgramParams, leftOffset, currentAngle, viewProjMatrix)

  private def drawCube(
      gl: WebGLRenderingContext,
      params: ProgramParams,
      leftOffset: Float,
      currentAngle: Float,
      viewProjMatrix: Matrix4
  ): Unit =
    val modelMatrix  = Matrix4.setTranslate(leftOffset, 0, 0)
      .rotate(20, 1, 0, 0)
      .rotate(currentAngle, 0, 1, 0)
    val normalMatrix = modelMatrix.invert.transpose
    gl.uniformMatrix4fv(params.uNormalMatrix, transpose = false, normalMatrix.toFloat32Array)
    val mvpMatrix    = viewProjMatrix * modelMatrix
    gl.uniformMatrix4fv(params.uMvpMatrix, transpose = false, mvpMatrix.toFloat32Array)
    gl.drawElements(WebGLRenderingContext.TRIANGLES, indices.length, WebGLRenderingContext.UNSIGNED_BYTE, offset = 0)

  private def lookupProgramParams(gl: WebGLRenderingContext, program: WebGLProgram): ProgramParams =
    ProgramParams(
      uNormalMatrix = gl.getUniformLocation(program, "u_NormalMatrix"),
      uMvpMatrix = gl.getUniformLocation(program, "u_MvpMatrix")
    )

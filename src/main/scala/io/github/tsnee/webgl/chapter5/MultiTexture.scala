package io.github.tsnee.webgl.chapter5

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object MultiTexture extends Exercise:
  override val label: String = "MultiTexture"

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec2 a_TexCoord;
varying vec2 v_TexCoord;
void main() {
  gl_Position = a_Position;
  v_TexCoord = a_TexCoord;
}
"""

  val fragmentShaderSource: String =
    """
precision mediump float;
uniform sampler2D u_Sampler0;
uniform sampler2D u_Sampler1;
varying vec2 v_TexCoord;
void main() {
  vec4 color0 = texture2D(u_Sampler0, v_TexCoord);
  vec4 color1 = texture2D(u_Sampler1, v_TexCoord);
  gl_FragColor = color0 * color1;
}
"""

  def initialize(canvas: Canvas): Unit =
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run
    )

  private def run(
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize         = Float32Array.BYTES_PER_ELEMENT
    val verticesTexCoords =
      Float32Array(js.Array(-0.5f, 0.5f, 0f, 1f, -0.5f, -0.5f, 0f, 0f, 0.5f, 0.5f, 1f, 1f, 0.5f, -0.5f, 1f, 0f))
    initializeVbo(gl, verticesTexCoords)
    enableFloatAttribute(gl, program, "a_Position", 2, floatSize * 4, 0)
    enableFloatAttribute(gl, program, "a_TexCoord", 2, floatSize * 4, floatSize * 2)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.useProgram(program)
    val uSampler0         = gl.getUniformLocation(program, "u_Sampler0")
    val uSampler1         = gl.getUniformLocation(program, "u_Sampler1")
    val texture0          = gl.createTexture()
    val texture1          = gl.createTexture()
    val image0            = Image()
    image0.addEventListener("load", loadTexture(gl, verticesTexCoords.size / 4, texture0, uSampler0, image0, 0))
    image0.src = "sky.jpg"
    val image1            = Image()
    image1.addEventListener("load", loadTexture(gl, verticesTexCoords.size / 4, texture1, uSampler1, image1, 1))
    image1.src = "circle.gif"

  private val loadMask = mutable.BitSet()

  private def loadTexture(
      gl: WebGLRenderingContext,
      numVertices: Int,
      texture: WebGLTexture,
      sampler: WebGLUniformLocation,
      image: Image,
      texUnit: Int
  ): js.Function1[UIEvent, Unit] =
    import WebGLRenderingContext.*
    _ =>
      gl.pixelStorei(UNPACK_FLIP_Y_WEBGL, 1)
      texUnit match
        case 0 => gl.activeTexture(TEXTURE0)
        case 1 => gl.activeTexture(TEXTURE1)
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      gl.texImage2D(TEXTURE_2D, 0, RGB, RGB, UNSIGNED_BYTE, image)
      gl.uniform1i(sampler, texUnit)
      loadMask.add(texUnit)
      if loadMask.subsetOf(Set(0, 1)) then
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
        gl.drawArrays(
          mode = WebGLRenderingContext.TRIANGLE_STRIP,
          first = 0,
          count = numVertices
        )

  private def initializeVbo(gl: WebGLRenderingContext, array: Float32Array): Unit =
    val vertexTexCoordsBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexTexCoordsBuffer)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, array, WebGLRenderingContext.STATIC_DRAW)

  private def enableFloatAttribute(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      attributeName: String,
      size: Int,
      stride: Int,
      offset: Int
  ): Unit =
    val attribute = gl.getAttribLocation(program, attributeName)
    gl.vertexAttribPointer(
      indx = attribute,
      size = size,
      `type` = WebGLRenderingContext.FLOAT,
      normalized = false,
      stride = stride,
      offset = offset
    )
    gl.enableVertexAttribArray(attribute)

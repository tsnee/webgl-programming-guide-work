package io.github.tsnee.webgl.chapter5

import io.github.tsnee.webgl.WebglInitializer
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object TexturedQuad:
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
uniform sampler2D u_Sampler;
varying vec2 v_TexCoord;
void main() {
  gl_FragColor = texture2D(u_Sampler, v_TexCoord);
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
    val uSampler          = gl.getUniformLocation(program, "u_Sampler")
    val texture           = gl.createTexture()
    val image             = Image()
    image.addEventListener("load", loadTexture(gl, verticesTexCoords.size / 4, texture, uSampler, image))
    image.src = "sky.jpg"

  private def loadTexture(
      gl: WebGLRenderingContext,
      numVertices: Int,
      texture: WebGLTexture,
      sampler: WebGLUniformLocation,
      image: Image
  ): js.Function1[UIEvent, Unit] =
    import WebGLRenderingContext.*
    _ =>
      gl.pixelStorei(UNPACK_FLIP_Y_WEBGL, 1)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      gl.texImage2D(TEXTURE_2D, 0, RGB, RGB, UNSIGNED_BYTE, image)
      gl.uniform1i(sampler, 0)
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

package io.github.tsnee.webgl.chapter5

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.iltotore.iron.constraint.all._
import io.github.tsnee.webgl._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.types._
import org.scalajs.dom.Image
import org.scalajs.dom.UIEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLTexture
import org.scalajs.dom.WebGLUniformLocation

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object MultiTexture:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec2 a_TexCoord;
varying vec2 v_TexCoord;
void main() {
  gl_Position = a_Position;
  v_TexCoord = a_TexCoord;
}
"""

  val fragmentShaderSource: FragmentShaderSource =
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

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val floatSize         = Float32Array.BYTES_PER_ELEMENT
    val verticesTexCoords =
      Float32Array(js.Array(-0.5f, 0.5f, 0f, 1f, -0.5f, -0.5f, 0f, 0f, 0.5f, 0.5f, 1f, 1f, 0.5f, -0.5f, 1f, 0f))
    VertexBufferObject.initializeVbo(gl, verticesTexCoords)
    WebglAttribute.enableFloatAttribute(gl, program, "a_Position", 2, floatSize * 4, 0)
    WebglAttribute.enableFloatAttribute(gl, program, "a_TexCoord", 2, floatSize * 4, floatSize * 2)
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

  private val imageLoadCounter = Var[Int](0)

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
      imageLoadCounter.update(_ + 1)
      // wait until both images have been loaded before drawing
      if imageLoadCounter.now() > 1 then
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
        gl.drawArrays(
          mode = WebGLRenderingContext.TRIANGLE_STRIP,
          first = 0,
          count = numVertices
        )

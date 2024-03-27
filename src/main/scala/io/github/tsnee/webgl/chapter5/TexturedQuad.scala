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

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object TexturedQuad:
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
uniform sampler2D u_Sampler;
varying vec2 v_TexCoord;
void main() {
  gl_FragColor = texture2D(u_Sampler, v_TexCoord);
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
      Float32Array(js.Array[Float](-0.5, 0.5, 0, 1, -0.5, -0.5, 0, 0, 0.5, 0.5, 1, 1, 0.5, -0.5, 1, 0))
    VertexBufferObject.initializeVbo(gl, verticesTexCoords)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 2, floatSize * 4, 0)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_TexCoord",
      2,
      floatSize * 4,
      floatSize * 2
    )
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.useProgram(program)
    val uSampler          = gl.getUniformLocation(program, "u_Sampler")
    val texture           = gl.createTexture()
    val image             = Image()
    image.addEventListener("load", TextureLoader.loadTexture(gl, verticesTexCoords.size / 4, texture, uSampler, image))
    image.src = "sky.jpg"

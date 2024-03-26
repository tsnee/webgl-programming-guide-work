package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L.{Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object HelloCube extends Exercise:
  override val label: String = "HelloCube"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_MvpMatrix;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  v_Color = a_Color;
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
    val floatSize      = Float32Array.BYTES_PER_ELEMENT
    // Create a cube
    //    v6----- v5
    //   /|      /|
    //  v1------v0|
    //  | |     | |
    //  | |v7---|-|v4
    //  |/      |/
    //  v2------v3
    val verticesColors = Float32Array(js.Array(
      1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,   // v0 White
      -1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f,  // v1 Magenta
      -1.0f, -1.0f, 1.0f, 1.0f, 0.0f, 0.0f, // v2 Red
      1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 0.0f,  // v3 Yellow
      1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // v4 Green
      1.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f,  // v5 Cyan
      -1.0f, 1.0f, -1.0f, 0.0f, 0.0f, 1.0f, // v6 Blue
      -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f // v7 Black
    ))
    initializeVbo(gl, verticesColors)
    val indices        = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3, // front
      0, 3, 4, 0, 4, 5, // right
      0, 5, 6, 0, 6, 1, // up
      1, 6, 7, 1, 7, 2, // left
      7, 4, 3, 7, 3, 2, // down
      4, 7, 6, 4, 6, 5  // back
    ))
    val indexBuffer    = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    enableFloatAttribute(gl, program, "a_Position", 3, floatSize * 6, 0)
    enableFloatAttribute(gl, program, "a_Color", 3, floatSize * 6, floatSize * 3)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val mvpMatrix      = Matrix4
      .setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
      .lookAt(eyeX = 3f, eyeY = 3f, eyeZ = 7f, atX = 0f, atY = 0f, atZ = 0f, upX = 0f, upY = 1f, upZ = 0f)
    val numIndices     = indices.size
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.uniformMatrix4fv(
      location = uMvpMatrix,
      transpose = false,
      value = mvpMatrix.toFloat32Array
    )
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = numIndices,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
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

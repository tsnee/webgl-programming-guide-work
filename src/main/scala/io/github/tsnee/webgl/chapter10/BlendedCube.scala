package io.github.tsnee.webgl.chapter10

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExercisePanelBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray._

object BlendedCube:
  val vertexShaderSource: VertexShaderSource =
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

  val fragmentShaderSource: FragmentShaderSource =
    """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  def panel(height: Height, width: Width): Element =
    ExercisePanelBuilder.buildPanelBuilder(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    // Create a cube
    //    v6----- v5
    //   /|      /|
    //  v1------v0|
    //  | |     | |
    //  | |v7---|-|v4
    //  |/      |/
    //  v2------v3
    val vertices    = Float32Array(js.Array[Float](
      1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,
      1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,
      1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,
      -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1,
      -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1,
      1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1
    ))
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 3, 0, 0)
    val colors      = Float32Array(js.Array[Float](
      0.5, 0.5, 1.0, 0.4, 0.5, 0.5, 1.0, 0.4, 0.5, 0.5, 1.0, 0.4, 0.5, 0.5, 1.0, 0.4, // v0-v1-v2-v3 front(blue)
      0.5, 1.0, 0.5, 0.4, 0.5, 1.0, 0.5, 0.4, 0.5, 1.0, 0.5, 0.4, 0.5, 1.0, 0.5, 0.4, // v0-v3-v4-v5 right(green)
      1.0, 0.5, 0.5, 0.4, 1.0, 0.5, 0.5, 0.4, 1.0, 0.5, 0.5, 0.4, 1.0, 0.5, 0.5, 0.4, // v0-v5-v6-v1 up(red)
      1.0, 1.0, 0.5, 0.4, 1.0, 1.0, 0.5, 0.4, 1.0, 1.0, 0.5, 0.4, 1.0, 1.0, 0.5, 0.4, // v1-v6-v7-v2 left
      1.0, 1.0, 1.0, 0.4, 1.0, 1.0, 1.0, 0.4, 1.0, 1.0, 1.0, 0.4, 1.0, 1.0, 1.0, 0.4, // v7-v4-v3-v2 down
      0.5, 1.0, 1.0, 0.4, 0.5, 1.0, 1.0, 0.4, 0.5, 1.0, 1.0, 0.4, 0.5, 1.0, 1.0, 0.4  // v4-v7-v6-v5 back
    ))
    VertexBufferObject.initializeVbo(gl, colors)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Color", 4, 0, 0)
    val indices     = Uint8Array(js.Array[Short](
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
    gl.clearColor(0, 0, 0, 1)
//    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.enable(WebGLRenderingContext.BLEND)
    gl.blendFunc(WebGLRenderingContext.SRC_ALPHA, WebGLRenderingContext.ONE_MINUS_SRC_ALPHA)
    gl.useProgram(program)
    val uMvpMatrix  = gl.getUniformLocation(program, "u_MvpMatrix")
    val mvpMatrix   = Matrix4
      .setPerspective(30, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
      .lookAt(eyeX = 3, eyeY = 3, eyeZ = 7, atX = 0, atY = 0, atZ = 0, upX = 0, upY = 1, upZ = 0)
    val numIndices  = indices.size
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

package io.github.tsnee.webgl.chapter8

import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.common.ExerciseBuilder
import io.github.tsnee.webgl.common.VertexBufferObject
import io.github.tsnee.webgl.common.WebglAttribute
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.math.Vec4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.{Element => _, _}

import scala.annotation.unused
import scala.scalajs.js
import scala.scalajs.js.typedarray._

object LightedTranslatedRotatedCube:
  val vertexShaderSource: VertexShaderSource =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec4 a_Normal;
uniform mat4 u_MvpMatrix;
uniform mat4 u_NormalMatrix;
uniform vec3 u_LightColor;
uniform vec3 u_LightDirection;
uniform vec3 u_AmbientLight;
varying vec4 v_Color;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  vec3 normal = normalize(vec3(u_NormalMatrix * a_Normal));
  float nDotL = max(dot(u_LightDirection, normal), 0.0);
  vec3 diffuse = u_LightColor * vec3(a_Color) * nDotL;
  vec3 ambient = u_AmbientLight * a_Color.rgb;
  v_Color = vec4(diffuse + ambient, a_Color.a);;
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
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(height, width)

  private def useWebgl(
      @unused canvas: Canvas,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    val vertices        = Float32Array(js.Array[Float](
      1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,
      1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,
      1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,
      -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1,
      -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1,
      1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1
    ))
    VertexBufferObject.initializeVbo(gl, vertices)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Position", 3, 0, 0)
    val colors          = Float32Array(js.Array[Float](
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0
    ))
    VertexBufferObject.initializeVbo(gl, colors)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Color", 3, 0, 0)
    val normals         = Float32Array(js.Array[Float](
      0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,     // v0-v1-v2-v3 front
      1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,     // v0-v3-v4-v5 right
      0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,     // v0-v5-v6-v1 up
      -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // v1-v6-v7-v2 left
      0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // v7-v4-v3-v2 down
      0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1  // v4-v7-v6-v5 back
    ))
    VertexBufferObject.initializeVbo(gl, normals)
    WebglAttribute.enableAttribute(gl, program, WebGLRenderingContext.FLOAT, "a_Normal", 3, 0, 0)
    val indices         = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3,       // front
      4, 5, 6, 4, 6, 7,       // right
      8, 9, 10, 8, 10, 11,    // up
      12, 13, 14, 12, 14, 15, // left
      16, 17, 18, 16, 18, 19, // down
      20, 21, 22, 20, 22, 23  // back
    ))
    val indexBuffer     = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.clearColor(0, 0, 0, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val uLightColor     = gl.getUniformLocation(program, "u_LightColor")
    gl.uniform3f(uLightColor, 1, 1, 1);
    val uLightDirection = gl.getUniformLocation(program, "u_LightDirection")
    val lightDirection  = Vec4.direction(0, 3, 4)
    gl.uniform3fv(uLightDirection, lightDirection.normal.to3dFloat32Array)
    val uNormalMatrix   = gl.getUniformLocation(program, "u_NormalMatrix")
    val modelMatrix     = Matrix4.setTranslate(0, 0.9, 0).rotate(90, 0, 0, 1)
    val normalMatrix    = modelMatrix.invert.transpose
    gl.uniformMatrix4fv(uNormalMatrix, transpose = false, normalMatrix.toFloat32Array)
    val uMvpMatrix      = gl.getUniformLocation(program, "u_MvpMatrix")
    val mvpMatrix       = Matrix4
      .setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
      .lookAt(eyeX = 3, eyeY = 3, eyeZ = 7, atX = 0, atY = 0, atZ = 0, upX = 0, upY = 1, upZ = 0) * modelMatrix
    val uAmbientLight   = gl.getUniformLocation(program, "u_AmbientLight")
    gl.uniform3f(uAmbientLight, 0.2, 0.2, 0.2)
    val numIndices      = indices.size
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

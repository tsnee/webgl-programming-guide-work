package io.github.tsnee.webgl.chapter8

import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object PointLightedCubePerFragment extends Exercise:
  override val label: String = "PointLightedCube_perFragment"

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec4 a_Normal;
uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_NormalMatrix;
varying vec4 v_Color;
varying vec3 v_Normal;
varying vec3 v_Position;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  v_Position = vec3(u_ModelMatrix * a_Position);
  v_Normal = normalize(vec3(u_NormalMatrix * a_Normal));
  v_Color = a_Color;
}
"""

  val fragmentShaderSource: String =
    """
precision mediump float;
uniform vec3 u_LightColor;
uniform vec3 u_LightPosition;
uniform vec3 u_AmbientLight;
varying vec3 v_Normal; // interpolated, so no longer normalized
varying vec3 v_Position;
varying vec4 v_Color;
void main() {
  vec3 normal = normalize(v_Normal);
  vec3 lightDirection = normalize(u_LightPosition - v_Position);
  float nDotL = max(dot(lightDirection, normal), 0.0);
  vec3 diffuse = u_LightColor * v_Color.rgb * nDotL;
  vec3 ambient = u_AmbientLight * v_Color.rgb;
  gl_FragColor = vec4(diffuse + ambient, v_Color.a);
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
    val vertices       = Float32Array(js.Array(
      2f, 2f, 2f, -2f, 2f, 2f, -2f, -2f, 2f, 2f, -2f, 2f,
      2f, 2f, 2f, 2f, -2f, 2f, 2f, -2f, -2f, 2f, 2f, -2f,
      2f, 2f, 2f, 2f, 2f, -2f, -2f, 2f, -2f, -2f, 2f, 2f,
      -2f, 2f, 2f, -2f, 2f, -2f, -2f, -2f, -2f, -2f, -2f, 2f,
      -2f, -2f, -2f, 2f, -2f, -2f, 2f, -2f, 2f, -2f, -2f, 2f,
      2f, -2f, -2f, -2f, -2f, -2f, -2f, 2f, -2f, 2f, 2f, -2f
    ))
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    val colors         = Float32Array(js.Array(
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f
    ))
    initializeVbo(gl, program, colors, 3, WebGLRenderingContext.FLOAT, "a_Color")
    val normals        = Float32Array(js.Array(
      0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f,     // v0-v1-v2-v3 front
      1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f,     // v0-v3-v4-v5 right
      0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 0f,     // v0-v5-v6-v1 up
      -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, // v1-v6-v7-v2 left
      0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, // v7-v4-v3-v2 down
      0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f, 0f, 0f, -1f  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, normals, 3, WebGLRenderingContext.FLOAT, "a_Normal")
    val indices        = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3,       // front
      4, 5, 6, 4, 6, 7,       // right
      8, 9, 10, 8, 10, 11,    // up
      12, 13, 14, 12, 14, 15, // left
      16, 17, 18, 16, 18, 19, // down
      20, 21, 22, 20, 22, 23  // back
    ))
    val indexBuffer    = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    gl.useProgram(program)
    val uModelMatrix   = gl.getUniformLocation(program, "u_ModelMatrix")
    val uMvpMatrix     = gl.getUniformLocation(program, "u_MvpMatrix")
    val uNormalMatrix  = gl.getUniformLocation(program, "u_NormalMatrix")
    val uLightColor    = gl.getUniformLocation(program, "u_LightColor")
    val uLightPosition = gl.getUniformLocation(program, "u_LightPosition")
    val uAmbientLight  = gl.getUniformLocation(program, "u_AmbientLight")
    gl.uniform3f(uLightColor, 1f, 1f, 1f)
    gl.uniform3f(uLightPosition, 2.3f, 4f, 3.5f)
    gl.uniform3f(uAmbientLight, 0.2f, 0.2f, 0.2f)
    val modelMatrix    = Matrix4.setRotate(90, 0, 1, 0)
    gl.uniformMatrix4fv(uModelMatrix, transpose = false, modelMatrix.toFloat32Array)
    val projMatrix     = Matrix4.setPerspective(30f, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 100)
    val viewMatrix     =
      Matrix4.setLookAt(eyeX = 6f, eyeY = 6f, eyeZ = 14f, atX = 0f, atY = 0f, atZ = 0f, upX = 0f, upY = 1f, upZ = 0f)
    val mvpMatrix      = projMatrix * viewMatrix * modelMatrix
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    val normalMatrix   = modelMatrix.invert.transpose
    gl.uniformMatrix4fv(uNormalMatrix, transpose = false, normalMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = indices.size,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )

  private def initializeVbo(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      array: Float32Array,
      size: Int,
      typ: Int,
      attributeName: String
  ): Unit =
    val vertexBufferObject = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexBufferObject)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, array, WebGLRenderingContext.STATIC_DRAW)
    val attribute          = gl.getAttribLocation(program, attributeName)
    gl.vertexAttribPointer(
      indx = attribute,
      size = size,
      `type` = typ,
      normalized = false,
      stride = 0,
      offset = 0
    )
    gl.enableVertexAttribArray(attribute)
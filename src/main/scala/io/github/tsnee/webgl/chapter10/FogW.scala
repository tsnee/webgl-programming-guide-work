package io.github.tsnee.webgl.chapter10

import com.raquo.laminar.api.L.{Element => _, Image => _, _}
import io.github.tsnee.webgl.Exercise
import io.github.tsnee.webgl.WebglInitializer
import io.github.tsnee.webgl.math.Matrix4
import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array
import scala.scalajs.js.typedarray.Uint8Array

object FogW extends Exercise:
  override val label: String = "Fog_w"

  lazy val panel: com.raquo.laminar.api.L.Element =
    val canvas = canvasTag(widthAttr := 400, heightAttr := 400)
    initialize(canvas.ref)
    div(canvas)

  override def height: Int = 500

  val vertexShaderSource: String =
    """
attribute vec4 a_Position;
attribute vec4 a_Color;
uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;
uniform vec4 u_Eye;
varying vec4 v_Color;
varying float v_Dist;
void main() {
  gl_Position = u_MvpMatrix * a_Position;
  v_Color = a_Color;
  v_Dist = gl_Position.w;
}
"""

  val fragmentShaderSource: String =
    """
precision mediump float;
uniform vec3 u_FogColor;
uniform vec2 u_FogDist;
varying vec4 v_Color;
varying float v_Dist;
void main() {
  float fogFactor = clamp((u_FogDist.y - v_Dist) / (u_FogDist.y - u_FogDist.x), 0.0, 1.0);
  vec3 color = mix(u_FogColor, v_Color.rgb, fogFactor);
  gl_FragColor = vec4(color, v_Color.a);
}
"""

  private val gFogDist = Array.ofDim[Float](2)

  def initialize(canvas: Canvas): Unit =
    Array[Float](55, 80).copyToArray(gFogDist)
    WebglInitializer.initialize(
      canvas,
      vertexShaderSource,
      fragmentShaderSource,
      run(canvas, _, _)
    )

  private def run(
      canvas: HTMLCanvasElement,
      gl: WebGLRenderingContext,
      program: WebGLProgram
  ): Unit =
    gl.useProgram(program)
    val uFogColor    = gl.getUniformLocation(program, "u_FogColor")
    val fogColor     = Float32Array(js.Array[Float](0.137, 0.231, 0.423))
    gl.uniform3fv(uFogColor, fogColor)
    val uFogDist     = gl.getUniformLocation(program, "u_FogDist")
    gl.uniform2fv(uFogDist, Float32Array(js.Array[Float](gFogDist*)))
    val uEye         = gl.getUniformLocation(program, "u_Eye")
    val eye          = Float32Array(js.Array[Float](25, 65, 35, 1))
    gl.uniform4fv(uEye, eye)
    val vertices     = Float32Array(js.Array[Float](
      1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1, 1,     // v0-v1-v2-v3 front
      1, 1, 1, 1, -1, 1, 1, -1, -1, 1, 1, -1,     // v0-v3-v4-v5 right
      1, 1, 1, 1, 1, -1, -1, 1, -1, -1, 1, 1,     // v0-v5-v6-v1 up
      -1, 1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1, // v1-v6-v7-v2 left
      -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1, // v7-v4-v3-v2 down
      1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, vertices, 3, WebGLRenderingContext.FLOAT, "a_Position")
    val colors       = Float32Array(js.Array[Float](
      0.4, 0.4, 1.0, 0.4, 0.4, 1.0, 0.4, 0.4, 1.0, 0.4, 0.4, 1.0, // v0-v1-v2-v3 front
      0.4, 1.0, 0.4, 0.4, 1.0, 0.4, 0.4, 1.0, 0.4, 0.4, 1.0, 0.4, // v0-v3-v4-v5 right
      1.0, 0.4, 0.4, 1.0, 0.4, 0.4, 1.0, 0.4, 0.4, 1.0, 0.4, 0.4, // v0-v5-v6-v1 up
      1.0, 1.0, 0.4, 1.0, 1.0, 0.4, 1.0, 1.0, 0.4, 1.0, 1.0, 0.4, // v1-v6-v7-v2 left
      1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, // v7-v4-v3-v2 down
      0.4, 1.0, 1.0, 0.4, 1.0, 1.0, 0.4, 1.0, 1.0, 0.4, 1.0, 1.0  // v4-v7-v6-v5 back
    ))
    initializeVbo(gl, program, colors, 3, WebGLRenderingContext.FLOAT, "a_Color")
    val indices      = Uint8Array(js.Array[Short](
      0, 1, 2, 0, 2, 3,       // front
      4, 5, 6, 4, 6, 7,       // right
      8, 9, 10, 8, 10, 11,    // up
      12, 13, 14, 12, 14, 15, // left
      16, 17, 18, 16, 18, 19, // down
      20, 21, 22, 20, 22, 23  // back
    ))
    val indexBuffer  = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indexBuffer)
    gl.bufferData(WebGLRenderingContext.ELEMENT_ARRAY_BUFFER, indices, WebGLRenderingContext.STATIC_DRAW)
    gl.clearColor(fogColor(0), fogColor(1), fogColor(2), 1f)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    val uMvpMatrix   = gl.getUniformLocation(program, "u_MvpMatrix")
    val uModelMatrix = gl.getUniformLocation(program, "u_ModelMatrix")
    val modelMatrix  = Matrix4.setScale(10, 10, 10)
    gl.uniformMatrix4fv(location = uModelMatrix, transpose = false, value = modelMatrix.toFloat32Array)
    val mvpMatrix    = Matrix4.setPerspective(30, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 1000)
      .lookAt(
        eyeX = eye(0),
        eyeY = eye(1),
        eyeZ = eye(2),
        atX = 0,
        atY = 2,
        atZ = 0,
        upX = 0,
        upY = 1,
        upZ = 0
      ) * modelMatrix
    gl.uniformMatrix4fv(location = uMvpMatrix, transpose = false, value = mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawElements(
      mode = WebGLRenderingContext.TRIANGLES,
      count = indices.size,
      `type` = WebGLRenderingContext.UNSIGNED_BYTE,
      offset = 0
    )
    canvas.setAttribute("tabindex", "0") // can't get focus to receive keyboard evts w/out this
    canvas.addEventListener(
      "keydown",
      (ev: KeyboardEvent) =>
        ev.preventDefault()
        ev.key match
          case KeyValue.ArrowUp                                => gFogDist(1) += 1
          case KeyValue.ArrowDown if gFogDist(1) > gFogDist(0) => gFogDist(1) -= 1
          case _                                               => ()
        gl.uniform2fv(uFogDist, Float32Array(js.Array[Float](gFogDist*)))
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
        gl.drawElements(
          mode = WebGLRenderingContext.TRIANGLES,
          count = indices.size,
          `type` = WebGLRenderingContext.UNSIGNED_BYTE,
          offset = 0
        )
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

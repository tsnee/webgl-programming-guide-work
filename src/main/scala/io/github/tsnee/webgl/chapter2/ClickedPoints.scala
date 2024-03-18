package io.github.tsnee.webgl.chapter2

import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ClickedPoints extends SimpleWebglProgram:
  override def vertexShaderSource: String = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  override def fragmentShaderSource: String = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  override def canvasId: String = "clicked-points"

  def click(
      ev: MouseEvent,
      gl: WebGLRenderingContext,
      canvas: Canvas,
      aPosition: Int
  ): Unit =
    val rect = canvas.getBoundingClientRect()
    val x    = ((ev.clientX - rect.left) - canvas.width / 2) / (canvas.width / 2)
    val y    = (canvas.height / 2 - (ev.clientY - rect.top)) / (canvas.height / 2)
    gl.vertexAttrib3fv(
      aPosition,
      Float32Array(js.Array(x.toFloat, y.toFloat, 0f))
    )
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  override def run(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val aPosition = gl.getAttribLocation(program, "a_Position")
    lookupCanvasElement(canvasId).foreach(canvas =>
      canvas.addEventListener("click", click(_, gl, canvas, aPosition))
    )
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)

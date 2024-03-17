package io.github.tsnee.webgl.chapter2

import com.raquo.laminar.api.L.{*, given}
import io.github.tsnee.webgl.*
import org.scalajs.dom
import org.scalajs.dom.{MouseEvent, WebGLProgram, WebGLRenderingContext}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ClickedPointsWithLaminar extends ShaderCreator with ProgramCreator:
  type Canvas = com.raquo.laminar.nodes.ReactiveHtmlElement[dom.html.Canvas]

  val vertexShaderSource: String = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
"""

  val fragmentShaderSource: String = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""

  def click(
    ev: MouseEvent,
    gl: WebGLRenderingContext,
    canvas: dom.html.Canvas,
    aPosition: Int
  ): Unit =
    val rect = canvas.getBoundingClientRect()
    val x = ((ev.clientX - rect.left) - canvas.width / 2) / (canvas.width / 2)
    val y = (canvas.height / 2 - (ev.clientY - rect.top)) / (canvas.height / 2)
    gl.vertexAttrib3fv(aPosition, Float32Array(js.Array(x.toFloat, y.toFloat, 0f)))
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  def main: Unit =
    val canvas = canvasTag(widthAttr := 300, heightAttr := 300)
    Option(canvas.ref.getContext("webgl")) match
      case None => dom.console.log("Cannot get webg context")
      case Some(gl: WebGLRenderingContext) =>
        for vertexShader <- createShader(gl, WebGLRenderingContext.VERTEX_SHADER, vertexShaderSource)
            fragmentShader <- createShader(gl, WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderSource)
            program <- createProgram(gl, vertexShader, fragmentShader)
        yield run(gl, program, canvas)
      case Some(unexpected) =>
        dom.console.log("Expected WebGLRenderingContext, found " + js.JSON.stringify(unexpected))

  def run(gl: WebGLRenderingContext, program: WebGLProgram, canvas: Canvas): Unit =
    val aPosition = gl.getAttribLocation(program, "a_Position")
    canvas.ref.addEventListener("click", click(_, gl, canvas.ref, aPosition))
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)
    val appContainer = dom.document.querySelector("#clicked-points-laminar")
    renderOnDomContentLoaded(appContainer, canvas)

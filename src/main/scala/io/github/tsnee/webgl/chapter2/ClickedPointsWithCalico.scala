package io.github.tsnee.webgl.chapter2

import calico.*
import calico.html.io.{*, given}
import cats.effect.*
import fs2.concurrent.SignallingRef
import fs2.dom.*
import io.github.tsnee.webgl.{ShaderCreator, ProgramCreator}
import org.scalajs.dom
import org.scalajs.dom.{WebGLProgram, WebGLRenderingContext}
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

final case class Point(x: Float, y: Float)

final case class ClickedPointsWithCalico(
  canvas: HtmlCanvasElement[IO],
  points: SignallingRef[IO, List[Point]],
)

object ClickedPointsWithCalico extends ShaderCreator with ProgramCreator:
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

  def redraw(
    points: List[Point],
    gl: WebGLRenderingContext,
    canvas: HtmlCanvasElement[IO],
    aPosition: Int
  ): Unit =
    points.foreach:
      point =>
        gl.vertexAttrib3fv(aPosition, Float32Array(js.Array(point.x, point.y, 0f)))
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
        gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  private def run(gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val aPosition = gl.getAttribLocation(program, "a_Position")
    gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
    gl.clearColor(0f, 0f, 0f, 1f)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
    gl.useProgram(program)

  private def normalizeCoordinates(ev: MouseEvent[IO], canvas: HtmlCanvasElement[IO]): Point =
    val rect = canvas.getBoundingClientRect()
    val x = ((ev.clientX - rect.left) - canvas.width / 2) / (canvas.width / 2)
    val y = (canvas.height / 2 - (ev.clientY - rect.top)) / (canvas.height / 2)
    Point(x, y)

  def component: Resource[IO, HtmlElement[IO]] =
    SignallingRef[IO].of(List.empty[Point]).toResource.flatMap:
      points =>
        canvasTag.withSelf:
          self =>
            Option(self.getContext("webgl")) match
              case None => dom.console.log("Cannot get webgl context from HTML Canvas element.")
              case Some(gl: WebGLRenderingContext) =>
                for vertexShader <- createShader(gl, WebGLRenderingContext.VERTEX_SHADER, vertexShaderSource)
                    fragmentShader <- createShader(gl, WebGLRenderingContext.FRAGMENT_SHADER, fragmentShaderSource)
                    program <- createProgram(gl, vertexShader, fragmentShader)
                yield run(gl, program)
              case Some(unexpected) =>
                dom.console.log("Expected webgl context of type WebGLRenderingContext, found " + JSON.stringify(unexpected))

            onClick --> (_.foreach:
              event =>
                val point = normalizeCoordinates(event, self)
                points.update(point :: _) *> redraw(points, self)
            )

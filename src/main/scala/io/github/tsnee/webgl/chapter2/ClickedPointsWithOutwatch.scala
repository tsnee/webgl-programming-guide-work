package io.github.tsnee.webgl.chapter2

import cats.*
import cats.effect.*
import cats.syntax.all.*
import colibri.effect.RunEffect
import io.github.tsnee.webgl.chapter2.EffectfulProgramCreator.createProgram
import io.github.tsnee.webgl.chapter2.EffectfulShaderCreator.createShader
import org.scalajs.dom
import org.scalajs.dom.MouseEvent
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLRenderingContext.FRAGMENT_SHADER
import org.scalajs.dom.html.*
import outwatch.*
import outwatch.dsl.*

import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ClickedPointsWithOutwatch:
  private val params = WebgletParameters(
    containerId = "clicked-points-outwatch",
    canvasWidth = 300,
    canvasHeight = 300,
    vertexShaderSource = """
attribute vec4 a_Position;
void main() {
  gl_Position = a_Position;
  gl_PointSize = 10.0;
}
""",
    fragmentShaderSource = """
void main() {
  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
"""
  )

  def log[F[_] : Sync](msg: String): F[Unit] = Sync[F].delay(dom.console.log(msg))

  def main[F[_] : RunEffect : Sync]: F[Unit] =
    val init = (gl: WebGLRenderingContext, program: WebGLProgram) =>
        Sync[F].delay:
          val aPosition = gl.getAttribLocation(program, "a_Position")
          gl.vertexAttrib3f(aPosition, 0f, 0f, 0f)
          gl.clearColor(0f, 0f, 0f, 1f)
          gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
          gl.useProgram(program)
    val modifiers = buildModifiers(params, init)
    val component = canvas(onClick.foreachEffect(handleClick(???, ???)) :: modifiers)
    val querySelector = s"#${params.containerId}"
    Outwatch.renderReplace[F](querySelector, component)

  def handleClick[F[_] : Sync](gl: WebGLRenderingContext, aPosition: Int)(ev: MouseEvent): F[Unit] =
    Sync[F].delay:
      val rect = ev.target match
        case e: Element => e.getBoundingClientRect()
      val x    =
        ((ev.clientX - rect.left) - params.canvasWidth / 2) / (params.canvasWidth / 2)
      val y    =
        (params.canvasHeight / 2 - (ev.clientY - rect.top)) / (params.canvasHeight / 2)
      gl.vertexAttrib3fv(
        aPosition,
        Float32Array(js.Array(x.toFloat, y.toFloat, 0f))
      )
      gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
      gl.drawArrays(WebGLRenderingContext.POINTS, 0, 1)

  private def buildModifiers[F[_] : RunEffect : Sync](
                                                       webgletParameters: WebgletParameters,
                                                       build: (WebGLRenderingContext, WebGLProgram) => F[Unit]
                                                     ): List[VMod] =
    List(
      width := s"${webgletParameters.canvasWidth}px",
      height := s"${webgletParameters.canvasHeight}px",
      onDomMount.foreachEffect:
        case canvas: Canvas =>
          buildWebgletFromCanvas(canvas, webgletParameters, build)
    )

  private def buildWebgletFromCanvas[F[_] : Sync](
                                                   canvas: Canvas,
                                                   webgletParameters: WebgletParameters,
                                                   build: (WebGLRenderingContext, WebGLProgram) => F[Unit]
                                                 ): F[Unit] =
    Option(canvas.getContext("webgl")) match
      case None =>
        log("Cannot get webgl context from Canvas element.")
      case Some(gl: WebGLRenderingContext) =>
        compile(gl, webgletParameters).flatMap(_.map(build(gl, _)).sequence)
      case Some(unexpected) =>
        log(s"Expected webgl context of type WebGLRenderingContext, found ${js.JSON.stringify(unexpected)}")

  private def compile[F[_] : Sync](
                                    gl: WebGLRenderingContext,
                                    webgletParameters: WebgletParameters
                                  ): F[Either[String, WebGLProgram]] =
    for
      vertexShaderResult <- createShader(
        gl,
        WebGLRenderingContext.VERTEX_SHADER,
        webgletParameters.vertexShaderSource
      )
      fragmentShaderResult <- createShader(
        gl,
        WebGLRenderingContext.FRAGMENT_SHADER,
        webgletParameters.fragmentShaderSource
      )
      program <- (vertexShaderResult, fragmentShaderResult) match
        case (Right(vertexShader), Right(fragmentShader)) =>
          createProgram(gl, vertexShader, fragmentShader)
        case _ => "A shader failed to compile".asLeft.pure[F]
    yield program

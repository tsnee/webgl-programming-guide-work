package io.github.tsnee.webgl.chapter2

import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

import scala.scalajs.js.JSON

trait SimpleWebglProgram extends CanvasLookup with ShaderCreator
    with ProgramCreator:
  def vertexShaderSource: String

  def fragmentShaderSource: String

  def canvasId: String

  def main: Unit =
    lookupCanvasElement(canvasId).flatMap(canvas =>
      Option(canvas.getContext("webgl"))
    ) match
      case None                            => dom.console.log(
          s"Cannot get webgl context from HTML Canvas element with ID $canvasId."
        )
      case Some(gl: WebGLRenderingContext) =>
        (for
          vertexShader   <- createShader(
                              gl,
                              WebGLRenderingContext.VERTEX_SHADER,
                              vertexShaderSource
                            )
          fragmentShader <- createShader(
                              gl,
                              WebGLRenderingContext.FRAGMENT_SHADER,
                              fragmentShaderSource
                            )
          program        <- createProgram(gl, vertexShader, fragmentShader)
        yield run(gl, program)).getOrElse(())
      case Some(unexpected)                =>
        dom.console.log(
          "Expected webgl context of type WebGLRenderingContext, found " + JSON.stringify(
            unexpected
          )
        )

  def run(gl: WebGLRenderingContext, program: WebGLProgram): Unit

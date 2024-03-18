package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.ProgramCreator.createProgram
import io.github.tsnee.webgl.ShaderCreator.createShader
import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas

import scala.scalajs.js.JSON

object WebglInitializer:
  def initialize(
      canvas: Canvas,
      vertexShaderSource: String,
      fragmentShaderSource: String,
      run: (WebGLRenderingContext, WebGLProgram) => Unit
  ): Unit =
    Option(canvas.getContext("webgl")) match
      case None                            =>
        dom.console.log("Cannot get webgl context from HTML Canvas element.")
      case Some(gl: WebGLRenderingContext) =>
        val result =
          for
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
          yield run(gl, program)
        result match
          case Left(error) => dom.console.log(error)
          case Right(())   => ()
      case Some(unexpected)                =>
        dom.console.log(
          "Expected webgl context of type WebGLRenderingContext, found " +
            JSON.stringify(unexpected)
        )

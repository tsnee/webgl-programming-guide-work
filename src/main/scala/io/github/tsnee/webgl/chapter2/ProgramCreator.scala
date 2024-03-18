package io.github.tsnee.webgl.chapter2

import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLShader

trait ProgramCreator:
  def createProgram(
      gl: WebGLRenderingContext,
      vertexShader: WebGLShader,
      fragmentShader: WebGLShader
  ): Option[WebGLProgram] =
    val program         = gl.createProgram()
    gl.attachShader(program, vertexShader)
    gl.attachShader(program, fragmentShader)
    gl.linkProgram(program)
    val linkStatus: Any = gl.getProgramParameter(
      program,
      WebGLRenderingContext.LINK_STATUS
    )
    linkStatus match
      case b: Boolean if b => program.some
      case _               =>
        dom.console.log(
          "Program failed to compile: " + gl.getProgramInfoLog(program)
        )
        gl.deleteProgram(program)
        None

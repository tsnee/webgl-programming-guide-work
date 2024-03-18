package io.github.tsnee.webgl

import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLShader

object ProgramCreator:
  def createProgram(
      gl: WebGLRenderingContext,
      vertexShader: WebGLShader,
      fragmentShader: WebGLShader
  ): Either[String, WebGLProgram] =
    val program         = gl.createProgram()
    gl.attachShader(program, vertexShader)
    gl.attachShader(program, fragmentShader)
    gl.linkProgram(program)
    val linkStatus: Any =
      gl.getProgramParameter(program, WebGLRenderingContext.LINK_STATUS)
    linkStatus match
      case b: Boolean if b => program.asRight
      case _               =>
        gl.deleteProgram(program)
        s"Program failed to compile: ${gl.getProgramInfoLog(program)}".asLeft

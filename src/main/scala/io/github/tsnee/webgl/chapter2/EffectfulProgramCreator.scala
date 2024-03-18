package io.github.tsnee.webgl.chapter2

import cats._
import cats.effect._
import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLShader

object EffectfulProgramCreator:
  def createProgram[F[_] : Sync](
      gl: WebGLRenderingContext,
      vertexShader: WebGLShader,
      fragmentShader: WebGLShader
  ): F[Either[String, WebGLProgram]] =
    Sync[F].delay:
      val program         = gl.createProgram()
      gl.attachShader(program, vertexShader)
      gl.attachShader(program, fragmentShader)
      gl.linkProgram(program)
      val linkStatus: Any = gl.getProgramParameter(
        program,
        WebGLRenderingContext.LINK_STATUS
      )
      linkStatus match
        case b: Boolean if b => program.asRight
        case _               =>
          gl.deleteShader(vertexShader)
          gl.deleteShader(fragmentShader)
          gl.deleteProgram(program)
          s"Program failed to compile: ${gl.getProgramInfoLog(program)}".asLeft

package io.github.tsnee.webgl.chapter2

import cats._
import cats.effect._
import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLShader

object EffectfulShaderCreator:
  def createShader[F[_] : Sync](
      gl: WebGLRenderingContext,
      typ: Int,
      source: String
  ): F[Either[String, WebGLShader]] =
    Sync[F].delay:
      val shader             = gl.createShader(typ)
      gl.shaderSource(shader, source)
      gl.compileShader(shader)
      val compileStatus: Any = gl.getShaderParameter(
        shader,
        WebGLRenderingContext.COMPILE_STATUS
      )
      compileStatus match
        case b: Boolean if b => shader.asRight
        case _               =>
          gl.deleteShader(shader)
          s"Shader type $typ failed to compile: ${gl.getShaderInfoLog(shader)}".asLeft

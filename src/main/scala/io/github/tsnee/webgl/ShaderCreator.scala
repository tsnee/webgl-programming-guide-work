package io.github.tsnee.webgl

import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLShader

object ShaderCreator:
  def createShader(
      gl: WebGLRenderingContext,
      typ: Int,
      source: String
  ): Either[String, WebGLShader] =
    val shader             = gl.createShader(typ)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    val compileStatus: Any =
      gl.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS)
    compileStatus match
      case b: Boolean if b => shader.asRight
      case _               =>
        gl.deleteShader(shader)
        s"Shader type $typ failed to compile: ${gl.getShaderInfoLog(shader)}".asLeft

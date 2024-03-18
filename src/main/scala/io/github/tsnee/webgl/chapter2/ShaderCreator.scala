package io.github.tsnee.webgl.chapter2

import cats.syntax.all._
import org.scalajs.dom
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLShader

trait ShaderCreator:
  def createShader(
      gl: WebGLRenderingContext,
      typ: Int,
      source: String
  ): Option[WebGLShader] =
    val shader             = gl.createShader(typ)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    val compileStatus: Any = gl.getShaderParameter(
      shader,
      WebGLRenderingContext.COMPILE_STATUS
    )
    compileStatus match
      case b: Boolean if b => shader.some
      case _               =>
        dom.console.log(
          s"Shader type $typ failed to compile: " + gl.getShaderInfoLog(shader)
        )
        gl.deleteShader(shader)
        None

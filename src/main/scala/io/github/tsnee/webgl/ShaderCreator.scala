package io.github.tsnee.webgl

import org.scalajs.dom
import org.scalajs.dom.{WebGLRenderingContext, WebGLShader}

trait ShaderCreator:
  def createShader(gl: WebGLRenderingContext, typ: Int, source: String): Option[WebGLShader] =
    val shader = gl.createShader(typ)
    gl.shaderSource(shader, source)
    gl.compileShader(shader)
    if gl.getShaderParameter(shader, WebGLRenderingContext.COMPILE_STATUS).asInstanceOf[Boolean] then
      Some(shader)
    else
      dom.console.log(s"Shader type $typ failed to compile: " + gl.getShaderInfoLog(shader))
      gl.deleteShader(shader)
      None

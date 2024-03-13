package io.github.tsnee.webgl

import org.scalajs.dom
import org.scalajs.dom.{WebGLProgram, WebGLRenderingContext, WebGLShader}

trait ProgramCreator:
  def createProgram(gl: WebGLRenderingContext, vertexShader: WebGLShader, fragmentShader: WebGLShader): Option[WebGLProgram] =
    val program = gl.createProgram()
    gl.attachShader(program, vertexShader)
    gl.attachShader(program, fragmentShader)
    gl.linkProgram(program)
    if gl.getProgramParameter(program, WebGLRenderingContext.LINK_STATUS).asInstanceOf[Boolean] then
      Some(program)
    else
      dom.console.log("Program failed to compile: " + gl.getProgramInfoLog(program))
      gl.deleteProgram(program)
      None

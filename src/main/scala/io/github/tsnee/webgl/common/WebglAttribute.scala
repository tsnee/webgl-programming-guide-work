package io.github.tsnee.webgl.common

import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object WebglAttribute:
  def enableAttribute(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      attributeType: Int,
      attributeName: String,
      size: Int,
      stride: Int,
      offset: Int
  ): Int =
    val attribute = gl.getAttribLocation(program, attributeName)
    gl.vertexAttribPointer(
      indx = attribute,
      size = size,
      `type` = attributeType,
      normalized = false,
      stride = stride,
      offset = offset
    )
    gl.enableVertexAttribArray(attribute)
    attribute

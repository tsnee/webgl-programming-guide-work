package io.github.tsnee.webgl.common

import org.scalajs.dom.WebGLRenderingContext

import scala.scalajs.js.typedarray.Float32Array

object VertexBufferObject:
  def initializeVbo(gl: WebGLRenderingContext, array: Float32Array): Unit =
    val vertexTexCoordsBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexTexCoordsBuffer)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, array, WebGLRenderingContext.STATIC_DRAW)

package io.github.tsnee.webgl.common

import org.scalajs.dom.WebGLBuffer
import org.scalajs.dom.WebGLRenderingContext

import scala.scalajs.js.typedarray.ArrayBufferView

object VertexBufferObject:
  def initializeVbo(gl: WebGLRenderingContext, array: ArrayBufferView): WebGLBuffer =
    val vertexBuffer = gl.createBuffer()
    gl.bindBuffer(WebGLRenderingContext.ARRAY_BUFFER, vertexBuffer)
    gl.bufferData(WebGLRenderingContext.ARRAY_BUFFER, array, WebGLRenderingContext.STATIC_DRAW)
    vertexBuffer

package io.github.tsnee.webgl.common

import org.scalajs.dom.Image
import org.scalajs.dom.UIEvent
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLTexture
import org.scalajs.dom.WebGLUniformLocation

import scala.scalajs.js

object TextureLoader:
  def loadTexture(
      gl: WebGLRenderingContext,
      numVertices: Int,
      texture: WebGLTexture,
      sampler: WebGLUniformLocation,
      image: Image
  ): js.Function1[UIEvent, Unit] =
    import WebGLRenderingContext.*
    _ =>
      gl.pixelStorei(UNPACK_FLIP_Y_WEBGL, 1)
      gl.activeTexture(TEXTURE0)
      gl.bindTexture(TEXTURE_2D, texture)
      gl.texParameteri(TEXTURE_2D, TEXTURE_MIN_FILTER, LINEAR)
      gl.texImage2D(TEXTURE_2D, 0, RGB, RGB, UNSIGNED_BYTE, image)
      gl.uniform1i(sampler, 0)
      gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
      gl.drawArrays(
        mode = WebGLRenderingContext.TRIANGLE_STRIP,
        first = 0,
        count = numVertices
      )

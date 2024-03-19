package io.github.tsnee.webgl.chapter2

import org.scalajs.dom._
import org.scalajs.dom.html.Canvas

import scala.scalajs.js
import scala.scalajs.js.JSON

object HelloCanvas:
  def initialize(canvas: Canvas): Unit =
    Option(canvas.getContext("webgl")) match
      case None                            =>
        console.log("Cannot get webgl context from HTML Canvas element.")
      case Some(gl: WebGLRenderingContext) =>
        gl.clearColor(0f, 0f, 0f, 1f)
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
      case Some(unexpected)                =>
        console.log(
          "Expected webgl context of type WebGLRenderingContext, found " + JSON.stringify(
            unexpected
          )
        )

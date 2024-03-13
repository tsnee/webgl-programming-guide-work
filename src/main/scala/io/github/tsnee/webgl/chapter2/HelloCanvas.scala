package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.CanvasLookup
import org.scalajs.dom
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.html.Canvas
import scala.scalajs.js.JSON

object HelloCanvas extends CanvasLookup:
  def main: Unit =
    lookupCanvasElement("hello-canvas").flatMap(canvas => Option(canvas.getContext("webgl"))) match
      case None => dom.console.log("Cannot get webgl context from HTML Canvas element named hello-canvas.")
      case Some(gl: WebGLRenderingContext) =>
        gl.clearColor(0f, 0f, 0f, 1f)
        gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
      case Some(unexpected) =>
        dom.console.log("Expected webgl context of type WebGLRenderingContext, found " + JSON.stringify(unexpected))

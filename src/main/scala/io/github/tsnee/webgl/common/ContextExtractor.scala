package io.github.tsnee.webgl.common

import cats.syntax.all._
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.WebGLRenderingContext

object ContextExtractor:
  def extractContext(canvas: HTMLCanvasElement): Either[String, WebGLRenderingContext] =
    Option(canvas.getContext("webgl")) match
      case None                            =>
        "Cannot get WebGL context from HTML Canvas element.".asLeft
      case Some(gl: WebGLRenderingContext) =>
        gl.asRight
      case Some(unexpected)                =>
        s"Expected canvas context of type WebGLRenderingContext, found $unexpected.".asLeft

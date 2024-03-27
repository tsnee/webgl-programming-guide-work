package io.github.tsnee.webgl.common

import cats.syntax.all._
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.WebGLRenderingContext

object ContextExtractor:
  def extractWebglContext(canvas: HTMLCanvasElement): Either[String, WebGLRenderingContext] =
    Option(canvas.getContext("webgl")) match
      case None                            =>
        "Cannot get WebGL context from HTML Canvas element.".asLeft
      case Some(gl: WebGLRenderingContext) =>
        gl.asRight
      case Some(unexpected)                =>
        s"Expected canvas context of type WebGLRenderingContext, found $unexpected.".asLeft

  def extract2dContext(canvas: HTMLCanvasElement): Either[String, CanvasRenderingContext2D] =
    Option(canvas.getContext("2d")) match
      case None                                =>
        "Cannot get 2D context from HTML Canvas element.".asLeft
      case Some(ctx: CanvasRenderingContext2D) =>
        ctx.asRight
      case Some(unexpected)                    =>
        s"Expected canvas context of type CanvasRenderingContext2D, found $unexpected.".asLeft

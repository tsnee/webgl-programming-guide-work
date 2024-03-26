package io.github.tsnee.webgl.chapter2

import cats._
import cats.syntax.all._
import com.raquo.laminar.api.L._
import io.github.tsnee.webgl.types._
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.WebGLRenderingContext

import scala.scalajs.js

object HelloCanvas:
  def panel(height: Height, width: Width): Element =
    val canvas = canvasTag(heightAttr := height, widthAttr := width)
    extractContext(canvas.ref) match
      case Right(gl)   =>
        useWebgl(gl)
        div(canvas)
      case Left(error) =>
        div(error)

  private def useWebgl(gl: WebGLRenderingContext): Unit =
    gl.clearColor(0, 0, 0, 1)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)

  private def extractContext(canvas: HTMLCanvasElement): Either[String, WebGLRenderingContext] =
    Option(canvas.getContext("webgl")) match
      case None                            =>
        "Cannot get WebGL context from HTML Canvas element.".asLeft
      case Some(gl: WebGLRenderingContext) =>
        gl.asRight
      case Some(unexpected)                =>
        s"Expected canvas context of type WebGLRenderingContext, found $unexpected.".asLeft

package io.github.tsnee.webgl.common

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.github.tsnee.webgl.types._
import org.scalajs.dom.Event
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object ExerciseBuilder:
  def createWebglCanvas(
      vertexShaderSource: VertexShaderSource,
      fragmentShaderSource: FragmentShaderSource,
      useWebgl: (Canvas, WebGLRenderingContext, WebGLProgram) => Unit
  ): PanelBuilder =
    (canvasWidth: Width, canvasHeight: Height) =>
      val webglCanvas      = canvasTag(widthAttr := canvasWidth, heightAttr := canvasHeight)
      webglCanvas.ref.addEventListener(
        "webglcontextrestored",
        _ => extractAndUseContext(webglCanvas, vertexShaderSource, fragmentShaderSource, useWebgl)
      )
      val successOrFailure =
        extractAndUseContext(webglCanvas, vertexShaderSource, fragmentShaderSource, useWebgl)
      successOrFailure match
        case Right(())   => div(webglCanvas)
        case Left(error) => div(error)

  private def extractAndUseContext(
      webglCanvas: Canvas,
      vertexShaderSource: VertexShaderSource,
      fragmentShaderSource: FragmentShaderSource,
      useWebgl: (Canvas, WebGLRenderingContext, WebGLProgram) => Unit
  ): Either[String, Unit] =
    for
      gl <- ContextExtractor.extractWebglContext(webglCanvas.ref)
      pg <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
    yield useWebgl(webglCanvas, gl, pg)

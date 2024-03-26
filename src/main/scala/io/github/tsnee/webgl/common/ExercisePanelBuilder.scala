package io.github.tsnee.webgl.common

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.github.tsnee.webgl.types._
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object ExercisePanelBuilder:
  def buildPanelBuilder(
      vertexShaderSource: VertexShaderSource,
      fragmentShaderSource: FragmentShaderSource,
      useWebgl: (Canvas, WebGLRenderingContext, WebGLProgram) => Unit
  ): PanelBuilder =
    (height: Height, width: Width) =>
      val canvas           = canvasTag(heightAttr := height, widthAttr := width)
      val successOrFailure =
        for
          gl <- ContextExtractor.extractContext(canvas.ref)
          pg <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
        yield useWebgl(canvas, gl, pg)
      successOrFailure match
        case Right(())   => div(canvas)
        case Left(error) => div(error)

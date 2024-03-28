package io.github.tsnee.webgl.common

import com.raquo.laminar.api.L._
import com.raquo.laminar.nodes.ReactiveHtmlElement
import io.github.tsnee.webgl.types._
import org.scalajs.dom.HTMLCanvasElement
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext

object ExerciseBuilder:
  def createWebglCanvas(
      vertexShaderSource: VertexShaderSource,
      fragmentShaderSource: FragmentShaderSource,
      useWebgl: (Canvas, WebGLRenderingContext, WebGLProgram) => Unit
  ): PanelBuilder =
    (height: Height, width: Width) =>
      val webglCanvas      = canvasTag(heightAttr := height, widthAttr := width)
      val successOrFailure =
        for
          gl <- ContextExtractor.extractWebglContext(webglCanvas.ref)
          pg <- ProgramCreator.createProgram(gl, vertexShaderSource, fragmentShaderSource)
        yield useWebgl(webglCanvas, gl, pg)
      successOrFailure match
        case Right(())   => div(webglCanvas)
        case Left(error) => div(error)

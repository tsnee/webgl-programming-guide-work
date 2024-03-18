package io.github.tsnee.webgl.chapter3

import io.github.tsnee.webgl.ChapterSummary.createPanel
import org.scalajs.dom
import org.scalajs.dom.WebGLRenderingContext

object Summary:
  def component: dom.Node =
    val row = dom.document.createElement("div")
    row.classList.add("flex")
    row.appendChild(createPanel("MultiPoint", MultiPoint.initialize))
    row.appendChild(createPanel(
      "HelloTriangle",
      HelloTriangle.initialize(_, WebGLRenderingContext.TRIANGLES)
    ))
    row.appendChild(createPanel(
      "HelloTriangle_LINES",
      HelloTriangle.initialize(_, WebGLRenderingContext.LINES)
    ))
    row.appendChild(createPanel(
      "HelloTriangle_LINE_STRIP",
      HelloTriangle.initialize(_, WebGLRenderingContext.LINE_STRIP)
    ))
    row.appendChild(createPanel(
      "HelloTriangle_LINE_LOOP",
      HelloTriangle.initialize(_, WebGLRenderingContext.LINE_LOOP)
    ))
    row.appendChild(createPanel(
      "HelloQuad",
      HelloQuad.initialize(_, WebGLRenderingContext.TRIANGLE_STRIP)
    ))
    row.appendChild(createPanel(
      "HelloQuad_FAN",
      HelloQuad.initialize(_, WebGLRenderingContext.TRIANGLE_FAN)
    ))
    row.appendChild(createPanel(
      "TranslatedTriangle",
      TranslatedTriangle.initialize
    ))
    row.appendChild(createPanel(
      "RotatedTriangle",
      RotatedTriangle.initialize
    ))
    row.appendChild(createPanel(
      "RotatedTriangle_Matrix",
      RotatedTriangleMatrix.initialize
    ))
    row.appendChild(createPanel(
      "ScaledTriangle_Matrix",
      ScaledTriangleMatrix.initialize
    ))
    row

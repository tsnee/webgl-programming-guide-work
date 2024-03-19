package io.github.tsnee.webgl.chapter3

import io.github.tsnee.webgl.Example
import org.scalajs.dom
import org.scalajs.dom.WebGLRenderingContext

import java.net.URI

object Summary:
  def examples: List[Example] = List(
    Example(
      "MultiPoint",
      MultiPoint.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch03/MultiPoint.html")
    ),
    Example(
      "HelloTriangle",
      HelloTriangle.initialize(_, WebGLRenderingContext.TRIANGLES),
      URI("https://rodger.global-linguist.com/webgl/ch03/HelloTriangle.html")
    ),
    Example(
      "HelloTriangle_LINES",
      HelloTriangle.initialize(_, WebGLRenderingContext.LINES),
      URI("https://rodger.global-linguist.com/webgl/ch03/HelloTriangle_LINES.html")
    ),
    Example(
      "HelloTriangle_LINE_STRIP",
      HelloTriangle.initialize(_, WebGLRenderingContext.LINE_STRIP),
      URI("https://rodger.global-linguist.com/webgl/ch03/HelloTriangle_LINE_STRIP.html")
    ),
    Example(
      "HelloTriangle_LINE_LOOP",
      HelloTriangle.initialize(_, WebGLRenderingContext.LINE_LOOP),
      URI("https://rodger.global-linguist.com/webgl/ch03/HelloTriangle_LINE_LOOP.html")
    ),
    Example(
      "HelloQuad",
      HelloQuad.initialize(_, WebGLRenderingContext.TRIANGLE_STRIP),
      URI("https://rodger.global-linguist.com/webgl/ch03/HelloQuad.html")
    ),
    Example(
      "HelloQuad_FAN",
      HelloQuad.initialize(_, WebGLRenderingContext.TRIANGLE_FAN),
      URI("https://rodger.global-linguist.com/webgl/ch03/HelloQuad_FAN.html")
    ),
    Example(
      "TranslatedTriangle",
      TranslatedTriangle.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch03/TranslatedTriangle.html")
    ),
    Example(
      "RotatedTriangle",
      RotatedTriangle.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch03/RotatedTriangle.html")
    ),
    Example(
      "RotatedTriangle_Matrix",
      RotatedTriangleMatrix.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch03/RotatedTriangle_Matrix.html")
    ),
    Example(
      "ScaledTriangle_Matrix",
      ScaledTriangleMatrix.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch03/ScaledTriangle_Matrix.html")
    )
  )

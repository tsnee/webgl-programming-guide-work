package io.github.tsnee.webgl.chapter4

import io.github.tsnee.webgl.Example

import java.net.URI

object Summary:
  def examples: List[Example] = List(
    Example(
      "RotatedTriangle_Matrix4",
      RotatedTriangleMatrix4.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch04/RotatedTriangle_Matrix4.html")
    ),
    Example(
      "RotatedTranslatedTriangle",
      RotatedTranslatedTriangle.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch04/RotatedTranslatedTriangle.html")
    ),
    Example(
      "RotatingTriangle",
      RotatingTriangle.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch04/RotatingTriangle.html")
    ),
    Example(
      "RotatingTranslatedTriangle",
      RotatingTranslatedTriangle.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch04/RotatingTranslatedTriangle.html")
    )
  )

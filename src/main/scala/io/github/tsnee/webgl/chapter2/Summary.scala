package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.Example

import java.net.URI

object Summary:
  def examples: List[Example] = List(
    Example(
      "HelloCanvas",
      HelloCanvas.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch02/HelloCanvas.html")
    ),
    Example(
      "HelloPoint1",
      HelloPoint1.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch02/HelloPoint1.html")
    ),
    Example(
      "HelloPoint2",
      HelloPoint2.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch02/HelloPoint2.html")
    ),
    Example(
      "ClickedPoints",
      ClickedPoints.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch02/ClickedPoints.html")
    ),
    Example(
      "ColoredPoints",
      ColoredPoints.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch02/ColoredPoints.html")
    )
  )

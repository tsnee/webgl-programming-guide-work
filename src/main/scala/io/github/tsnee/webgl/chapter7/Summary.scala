package io.github.tsnee.webgl.chapter7

import io.github.tsnee.webgl.Example

import java.net.URI

object Summary:
  def examples: List[Example] = List(
    Example(
      "LookAtTriangles",
      LookAtTriangles.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch07/LookAtTriangles.html")
    )
  )

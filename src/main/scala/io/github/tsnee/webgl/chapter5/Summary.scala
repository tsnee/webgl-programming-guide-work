package io.github.tsnee.webgl.chapter5

import io.github.tsnee.webgl.Example

import java.net.URI

object Summary:
  def examples: List[Example] = List(
    Example(
      "MultiAttributeSize",
      MultiAttributeSize.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/MultiAttributeSize.html")
    ),
    Example(
      "MultiAttributeSize_Interleaved",
      MultiAttributeSizeInterleaved.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/MultiAttributeSize_Interleaved.html")
    ),
    Example(
      "MultiAttributeColor",
      MultiAttributeColor.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/MultiAttributeColor.html")
    ),
    Example(
      "ColoredTriangle",
      ColoredTriangle.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/ColoredTriangle.html")
    ),
    Example(
      "HelloTriangle_FragCoord",
      HelloTriangleFragCoord.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/HelloTriangle_FragCoord.html")
    ),
    Example(
      "TexturedQuad",
      TexturedQuad.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/TexturedQuad.html")
    ),
    Example(
      "TexturedQuad_Repeat",
      TexturedQuadRepeat.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/TexturedQuad_Repeat.html")
    ),
    Example(
      "TexturedQuad_Clamp_Mirror",
      TexturedQuadClampMirror.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/TexturedQuad_Clamp_Mirror.html")
    ),
    Example(
      "MultiTexture",
      MultiTexture.initialize,
      URI("https://rodger.global-linguist.com/webgl/ch05/MultiTexture.html")
    )
  )

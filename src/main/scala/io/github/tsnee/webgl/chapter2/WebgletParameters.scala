package io.github.tsnee.webgl.chapter2

final case class WebgletParameters(
    containerId: String,
    canvasWidth: Int,
    canvasHeight: Int,
    vertexShaderSource: String,
    fragmentShaderSource: String
)

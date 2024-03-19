package io.github.tsnee.webgl

import org.scalajs.dom.HTMLCanvasElement

import java.net.URI

final case class Example(
    title: String,
    activate: HTMLCanvasElement => Unit,
    originalJsPageUri: URI
)

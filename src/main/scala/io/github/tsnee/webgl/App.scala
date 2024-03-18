package io.github.tsnee.webgl

import org.scalajs.dom

@main def run(): Unit =
  val _ = dom.document.body.appendChild(chapter2.Summary.component)

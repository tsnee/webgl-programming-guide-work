package io.github.tsnee.webgl

import org.scalajs.dom
import org.scalajs.dom.html.Canvas

trait CanvasLookup:
  def lookupCanvasElement(elementName: String): Option[Canvas] =
    Option(dom.document.querySelector(s"#$elementName").asInstanceOf[Canvas])

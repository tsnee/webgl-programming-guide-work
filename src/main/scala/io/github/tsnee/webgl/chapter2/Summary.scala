package io.github.tsnee.webgl.chapter2

import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.dom.html.Canvas

object Summary:
  private val canvasWidth  = 300
  private val canvasHeight = 300

  def component: dom.Node =
    val row = dom.document.createElement("div")
    row.classList.add("flex")
    row.appendChild(createPanel("HelloCanvas", HelloCanvas.initialize))
    row.appendChild(createPanel("HelloPoint1", HelloPoint1.initialize))
    row.appendChild(createPanel("HelloPoint2", HelloPoint2.initialize))
    row.appendChild(createPanel("ClickedPoints", ClickedPoints.initialize))
    row.appendChild(createPanel("ColoredPoints", ColoredPoints.initialize))
    row

  private def createPanel(label: String, init: Canvas => Unit): Element =
    val div = dom.document.createElement("div")
    div.appendChild(createCanvas(init))
    div.appendChild(createP(label))
    div

  private def createCanvas(init: Canvas => Unit): Canvas =
    dom.document.createElement("canvas") match
      case canvas: Canvas =>
        canvas.width = canvasWidth
        canvas.height = canvasHeight
        init(canvas)
        canvas

private def createP(text: String): Element =
  val p = dom.document.createElement("p")
  p.innerText = text
  p

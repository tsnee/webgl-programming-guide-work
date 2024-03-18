package io.github.tsnee.webgl.chapter2

import io.github.tsnee.webgl.ChapterSummary.createPanel
import org.scalajs.dom._

object Summary:
  def component: Node =
    val row = document.createElement("div")
    row.classList.add("flex")
    row.appendChild(createPanel("HelloCanvas", HelloCanvas.initialize))
    row.appendChild(createPanel("HelloPoint1", HelloPoint1.initialize))
    row.appendChild(createPanel("HelloPoint2", HelloPoint2.initialize))
    row.appendChild(createPanel("ClickedPoints", ClickedPoints.initialize))
    row.appendChild(createPanel("ColoredPoints", ColoredPoints.initialize))
    row

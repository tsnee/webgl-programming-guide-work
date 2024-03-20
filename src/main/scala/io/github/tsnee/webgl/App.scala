package io.github.tsnee.webgl

import org.scalajs.dom._

import scala.scalajs.js

object App:
  val navPanelId = "navigation"
  val canvasId   = "app"

  @main def run(): Unit =
    (
      Option(document.querySelector(s"#$navPanelId")),
      Option(document.querySelector(s"iframe"))
    ) match
      case (Some(navPanel), Some(iframe: HTMLIFrameElement)) =>
        navPanel.appendChild(document.createElement("ul")) match
          case ul =>
            appendChapter(ul, iframe, 2, chapter2.Summary.examples)
            appendChapter(ul, iframe, 3, chapter3.Summary.examples)
            appendChapter(ul, iframe, 4, chapter4.Summary.examples)
            appendChapter(ul, iframe, 5, chapter5.Summary.examples)
            appendChapter(ul, iframe, 7, chapter7.Summary.examples)
      case _                                                 =>
        val errorMessage = document.createElement("span")
        errorMessage.innerText = s"This HTML document doesn't have the right elements."
        val _            = document.body.appendChild(errorMessage)

  private def appendChapter(
      outerUl: Node,
      iframe: HTMLIFrameElement,
      n: Int,
      examples: List[Example]
  ): Unit =
    outerUl.appendChild(document.createElement("li")) match
      case outerLi: HTMLLIElement =>
        outerLi.classList.add("parent")
        outerLi.appendChild(document.createElement("span")).innerText = s"Chapter $n"
        outerLi.appendChild(document.createElement("ul")) match
          case innerUl: HTMLUListElement =>
            innerUl.classList.add("child")
            examples.foreach(appendExample(innerUl, iframe))

  private def appendExample(ul: Node, iframe: HTMLIFrameElement)(example: Example): Unit =
    val innerLi = ul.appendChild(document.createElement("li"))
    innerLi.appendChild(document.createElement("span")) match
      case span =>
        span.innerText = example.title
        span.addEventListener("click", onClick(example, iframe))

  private def onClick(example: Example, iframe: HTMLIFrameElement): js.Function1[Event, Unit] =
    _ =>
      iframe.src = example.originalJsPageUri.toASCIIString
      Option(document.querySelector(s"#$canvasId")) match
        case Some(oldCanvas) =>
          document.createElement("canvas") match
            case newCanvas: HTMLCanvasElement =>
              newCanvas.id = canvasId
              newCanvas.width = 400
              newCanvas.height = 400
              oldCanvas.replaceWith(newCanvas)
              example.activate(newCanvas)
        case _               =>
          val errorMessage = document.createElement("span")
          errorMessage.innerText = s"This HTML document doesn't have the right elements."
          val _            = document.body.appendChild(errorMessage)

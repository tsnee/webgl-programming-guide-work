package io.github.tsnee.webgl

import org.scalajs.dom._

@main def run(): Unit =
  appendChapter(2, chapter2.Summary.component)
  appendChapter(3, chapter3.Summary.component)

private def appendChapter(n: Int, node: Node): Unit =
  val chapterContainer = document.createElement("fieldset")
  val chapterTitle     = document.createElement("legend")
  chapterTitle.innerText = s"Chapter $n"
  val _                = chapterContainer.appendChild(chapterTitle)
  val _                = chapterContainer.appendChild(node)
  val _                = document.body.appendChild(chapterContainer)

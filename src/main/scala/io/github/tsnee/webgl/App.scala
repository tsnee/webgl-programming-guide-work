package io.github.tsnee.webgl

import org.scalajs.dom._

import scala.scalajs.js

object App:
  private val exerciseContainerId = "scala-js"
  private val origialContainerId  = "js"

  @main def run(): Unit =
    (
      Option(document.querySelector("nav")),
      Option(document.querySelector(s"#$exerciseContainerId")),
      Option(document.querySelector(s"#$origialContainerId"))
    ) match
      case (Some(navPanel), Some(exerciseComponent), Some(originalComponent)) =>
        val ul = navPanel.appendChild(document.createElement("ul"))
        appendChapter(ul, exerciseComponent, originalComponent, chapter2.Summary)
        appendChapter(ul, exerciseComponent, originalComponent, chapter3.Summary)
        appendChapter(ul, exerciseComponent, originalComponent, chapter4.Summary)
        appendChapter(ul, exerciseComponent, originalComponent, chapter5.Summary)
        appendChapter(ul, exerciseComponent, originalComponent, chapter7.Summary)
        appendChapter(ul, exerciseComponent, originalComponent, chapter8.Summary)
        appendChapter(ul, exerciseComponent, originalComponent, chapter9.Summary)
//        appendChapter(ul, exerciseComponent, originalComponent, chapter10.Summary)
      case _                                                                  =>
        val errorMessage = document.createElement("h1")
        errorMessage.innerText = s"This HTML document doesn't have the right elements."
        val _            = document.body.appendChild(errorMessage)

  private def appendChapter(
      outerUl: Node,
      exerciseComponent: Element,
      originalComponent: Element,
      summary: ChapterSummary
  ): Unit =
    outerUl.appendChild(document.createElement("li")) match
      case outerLi: Element =>
        outerLi.classList.add("parent")
        outerLi.appendChild(document.createElement("span")).innerText = s"Chapter ${summary.chapter}"
        outerLi.appendChild(document.createElement("ul")) match
          case innerUl: Element =>
            innerUl.classList.add("child")
            summary.exercises.foreach(appendExample(innerUl, exerciseComponent, originalComponent, summary.chapter))

  private def appendExample(
      ul: Node,
      exerciseComponent: Element,
      originalComponent: Element,
      chapter: Int
  )(exercise: Exercise): Unit =
    val innerLi = ul.appendChild(document.createElement("li"))
    val span    = innerLi.appendChild(document.createElement("span"))
    span.innerText = exercise.label
    span.addEventListener("click", onClick(chapter, exercise, exerciseComponent, originalComponent))

  private def onClick(
      chapter: Int,
      exercise: Exercise,
      exerciseContainer: Element,
      originalContainer: Element
  ): js.Function1[Event, Unit] =
    _ =>
      (
        Option(exerciseContainer.querySelector(s"#${Exercise.componentId}")),
        Option(originalContainer.querySelector("iframe"))
      ) match
        case (Some(exerciseComponent), Some(iframe: HTMLIFrameElement)) =>
          iframe.width = exercise.width.toString
          iframe.height = exercise.height.toString
          iframe.src = originalUrl(chapter, exercise.label)
          val newComponent = exercise.build
          exerciseComponent.replaceWith(newComponent)
          newComponent.setAttribute("id", Exercise.componentId)
          newComponent.setAttribute(
            "style",
            s"width: ${exercise.width.toString}px; height: ${exercise.height.toString}px;"
          )
        case _                                                          =>
          val errorMessage = document.createElement("h1")
          errorMessage.innerText = s"This HTML document doesn't have the right elements."
          val _            = document.body.appendChild(errorMessage)

  private def originalUrl(chapter: Int, basename: String): String =
    val chapterPart = f"ch$chapter%02d" // left-pad with at most one '0' e.g. "ch05"
    val filename    = s"$basename.html"
    s"https://rodger.global-linguist.com/webgl/$chapterPart/$filename"

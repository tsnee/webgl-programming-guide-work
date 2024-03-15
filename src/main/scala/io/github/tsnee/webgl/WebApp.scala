package io.github.tsnee.webgl

import calico.*
import calico.html.io.{*, given}
import cats.effect.*
import fs2.dom.*
import io.github.tsnee.webgl.chapter2.ClickedPointsWithCalico

object WebApp extends IOWebApp:
  def render: Resource[IO, HtmlElement[IO]] = ClickedPointsWithCalico.component

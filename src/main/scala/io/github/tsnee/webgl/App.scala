package io.github.tsnee.webgl

import cats.effect._

object App extends IOApp.Simple:
  def run: IO[Unit] =
    val old = IO.delay:
      chapter2.HelloCanvas.main
      chapter2.HelloPoint1.main
      chapter2.HelloPoint2.main
      chapter2.ClickedPoints.main
      chapter2.ColoredPoints.main
    old *> chapter2.ClickedPointsWithOutwatch.main
    // chapter2.ColoredPointsWithOutwatch.main

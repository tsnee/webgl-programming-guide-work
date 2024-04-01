package io.github.tsnee.webgl.chapter10

import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FileDownloader:
  def downloadFile[T](filename: String, onSuccess: String => T): Future[T] =
    dom.fetch(filename).toFuture.flatMap(callback).map(onSuccess)

  def downloadFileFuture[T](filename: String, onSuccess: String => Future[T]): Future[T] =
    dom.fetch(filename).toFuture.flatMap(callback).flatMap(onSuccess)

  private def callback(response: dom.Response): Future[String] =
    response.text().toFuture

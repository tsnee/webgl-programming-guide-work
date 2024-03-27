package io.github.tsnee.webgl.chapter7

object OrthoView extends AbstractOrthoView:
  override val orthoProjectionLeft: Float   = -1f
  override val orthoProjectionRight: Float  = 1f
  override val orthoProjectionBottom: Float = -1f
  override val orthoProjectionTop: Float    = 1f

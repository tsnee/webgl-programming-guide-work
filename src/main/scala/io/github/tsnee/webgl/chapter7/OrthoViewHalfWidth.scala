package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L.{Element => _}
import com.raquo.laminar.api.L.{Image => _}

object OrthoViewHalfWidth extends AbstractOrthoView:
  override val orthoProjectionLeft: Float   = -0.3f
  override val orthoProjectionRight: Float  = 0.3f
  override val orthoProjectionBottom: Float = -1f
  override val orthoProjectionTop: Float    = 1f

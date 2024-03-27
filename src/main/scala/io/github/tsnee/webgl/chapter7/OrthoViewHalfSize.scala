package io.github.tsnee.webgl.chapter7

import com.raquo.laminar.api.L.{Element => _}
import com.raquo.laminar.api.L.{Image => _}

object OrthoViewHalfSize extends AbstractOrthoView:
  override val orthoProjectionLeft: Float   = -0.5f
  override val orthoProjectionRight: Float  = 0.5f
  override val orthoProjectionBottom: Float = -0.5f
  override val orthoProjectionTop: Float    = 0.5f

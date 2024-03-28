package io.github.tsnee.webgl.common

import com.raquo.laminar.api.L._
import io.github.tsnee.webgl.types.Height
import io.github.tsnee.webgl.types.Width

trait PanelBuilder:
  def apply(width: Width, height: Height): Element

package io.github.tsnee.webgl.chapter10.obj

object types:
  type VertexIndex  = Int
  type TextureIndex = Int
  type NormalIndex  = Int
  type ObjVertex    = (VertexIndex, Option[TextureIndex], Option[NormalIndex])
  type Float3d      = (Float, Float, Float)
  type Float4d      = (Float, Float, Float, Float)

package io.github.tsnee.webgl.chapter10.obj

import io.github.tsnee.webgl.chapter10.obj.types._

final case class ObjObject(
    name: String,
    vertices: Vector[Float4d],
    textures: Vector[Float3d],
    normals: Vector[Float3d],
    materialGroups: Map[String, Vector[Face]]
)

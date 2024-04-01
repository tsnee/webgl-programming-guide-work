package io.github.tsnee.webgl.chapter10.obj

final case class ObjDoc(
    mtls: Map[String, MtlDoc],
    objects: Map[String, ObjObject]
)

object ObjDoc:
  def lookupColor(objDoc: ObjDoc, materialName: String): Option[(Float, Float, Float, Float)] =
    val materialOpt = objDoc.mtls.view.values.collectFirst:
      case mtlDoc if mtlDoc.materials.contains(materialName) => mtlDoc.materials(materialName)
    materialOpt.map(material => (material.r, material.g, material.b, 1))

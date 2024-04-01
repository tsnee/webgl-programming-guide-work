package io.github.tsnee.webgl.chapter10

import cats.syntax.all._
import com.raquo.laminar.api.L._
import io.github.iltotore.iron._
import io.github.tsnee.webgl.chapter10.obj._
import io.github.tsnee.webgl.chapter10.obj.types._
import io.github.tsnee.webgl.common._
import io.github.tsnee.webgl.math.Matrix4
import io.github.tsnee.webgl.math.Vec4
import io.github.tsnee.webgl.types._
import org.scalajs.dom.WebGLProgram
import org.scalajs.dom.WebGLRenderingContext
import org.scalajs.dom.WebGLUniformLocation
import org.scalajs.dom.console
import org.scalajs.dom.window

import scala.annotation.unused
import scala.collection.immutable
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.Float32Array

object ObjViewer:
  private val vertexShaderSource: VertexShaderSource = """
attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec4 a_Normal;
uniform mat4 u_MvpMatrix;
uniform mat4 u_NormalMatrix;
varying vec4 v_Color;
mat4 mvpMatrix;
void main() {
  vec3 lightDirection = vec3(-0.35, 0.35, 0.87);
  gl_Position = u_MvpMatrix * a_Position;
  vec3 normal = normalize(vec3(u_NormalMatrix * a_Normal));
  float nDotL = max(dot(normal, lightDirection), 0.0);
  v_Color = vec4(a_Color.rgb * nDotL, a_Color.a);
}
"""

  private val fragmentShaderSource: FragmentShaderSource = """
precision mediump float;
varying vec4 v_Color;
void main() {
  gl_FragColor = v_Color;
}
"""

  def panel(canvasWidth: Width, canvasHeight: Height): Element =
    ExerciseBuilder.createWebglCanvas(vertexShaderSource, fragmentShaderSource, useWebgl)(canvasWidth, canvasHeight)

  private def useWebgl(@unused canvas: Canvas, gl: WebGLRenderingContext, program: WebGLProgram): Unit =
    val _ = readFile("cube.obj").map:
      case Left(error)   => console.log(error)
      case Right(objDoc) => handleFile(gl, program, objDoc)

  private def readFile(objFileName: String): Future[Either[String, ObjDoc]] =
    ObjParser.parseObj(objFileName)

  private def handleFile(gl: WebGLRenderingContext, program: WebGLProgram, objDoc: ObjDoc): Unit =
    gl.useProgram(program)
    gl.clearColor(0.2, 0.2, 0.2, 1)
    gl.enable(WebGLRenderingContext.DEPTH_TEST)
    val viewProjMatrix = Matrix4.setPerspective(30, gl.drawingBufferWidth.toFloat / gl.drawingBufferHeight, 1, 5000)
      .lookAt(0, 500, 200, 0, 0, 0, 0, 1, 0)
    objDoc.objects.view.values.foreach(drawObject(gl, program, objDoc, viewProjMatrix, _))

  private def drawObject(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      objDoc: ObjDoc,
      viewProjMatrix: Matrix4,
      objObject: ObjObject
  ): Unit =
    val (vertices, texturesOpt, normalsOpt, colorsOpt) = translateObjToWebgl(objDoc, objObject)
    prepareVertices(gl, program, vertices)
    texturesOpt.foreach(prepareTextures(gl, program, _))
    prepareNormals(gl, program, normalsOpt, vertices)
    colorsOpt.foreach(prepareColors(gl, program, _))
    val uNormalMatrix                                  = gl.getUniformLocation(program, "u_NormalMatrix")
    val uMvpMatrix                                     = gl.getUniformLocation(program, "u_MvpMatrix")
    val now                                            = js.Date.now()
    val _                                              =
      tick(gl, program, vertices.size, uNormalMatrix, uMvpMatrix, viewProjMatrix, currentAngle = 0, prevTs = now)(now)

  private def tick(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      numVertices: Int,
      uNormalMatrix: WebGLUniformLocation,
      uMvpMatrix: WebGLUniformLocation,
      viewProjMatrix: Matrix4,
      currentAngle: Float,
      prevTs: Double
  )(curTs: Double): Int =
    val nextAngle    = animate(currentAngle, prevTs, curTs)
    val modelMatrix  = Matrix4.setScale(60, 60, 60)
      .rotate(nextAngle, 1, 0, 0)
      .rotate(nextAngle, 0, 1, 0)
      .rotate(nextAngle, 0, 0, 1)
    val normalMatrix = modelMatrix.invert.transpose
    gl.uniformMatrix4fv(uNormalMatrix, transpose = false, normalMatrix.toFloat32Array)
    val mvpMatrix    = viewProjMatrix * modelMatrix
    gl.uniformMatrix4fv(uMvpMatrix, transpose = false, mvpMatrix.toFloat32Array)
    gl.clear(WebGLRenderingContext.COLOR_BUFFER_BIT | WebGLRenderingContext.DEPTH_BUFFER_BIT)
    gl.drawArrays(WebGLRenderingContext.TRIANGLES, first = 0, count = numVertices)
    window.requestAnimationFrame(tick(
      gl,
      program,
      numVertices,
      uNormalMatrix,
      uMvpMatrix,
      viewProjMatrix,
      nextAngle,
      curTs
    )(_))

  private val angleStepDegrees = 30

  private def animate(currentAngle: Float, curTs: Double, prevTs: Double): Float =
    val elapsed = curTs - prevTs
    (currentAngle + (angleStepDegrees * elapsed / 1000).toFloat) % 360

  private def translateObjToWebgl(
      objDoc: ObjDoc,
      objObject: ObjObject
  ): (Vector[Float4d], Option[Vector[Float3d]], Option[Vector[Float3d]], Option[Vector[Float4d]]) =
    val vertices    = mutable.Buffer[Float4d]()
    val textureOpts = mutable.Buffer[Option[Float3d]]()
    val normalOpts  = mutable.Buffer[Option[Float3d]]()
    val colorOpts   = mutable.Buffer[Option[Float4d]]()
    objObject.materialGroups.foreach: (materialName, faces) =>
      val colorOpt = ObjDoc.lookupColor(objDoc, materialName)
      faces.foreach: face =>
        face.indices match
          case Vector(vtn0, vtn1, vtn2)       =>
            addTriangle(objObject, vtn0, vtn1, vtn2, colorOpt, vertices, textureOpts, normalOpts, colorOpts)
          case Vector(vtn0, vtn1, vtn2, vtn3) =>
            addTriangle(objObject, vtn0, vtn1, vtn2, colorOpt, vertices, textureOpts, normalOpts, colorOpts)
            addTriangle(objObject, vtn0, vtn2, vtn3, colorOpt, vertices, textureOpts, normalOpts, colorOpts)
          case unsupported                    =>
            console.log(
              s"This code only handles object faces with 3 or 4 sides, but this file has one with ${unsupported.size} sides."
            )
    (vertices.toVector, textureOpts.toVector.sequence, normalOpts.toVector.sequence, colorOpts.toVector.sequence)

  private def addTriangle(
      objObject: ObjObject,
      vtn0: ObjVertex,
      vtn1: ObjVertex,
      vtn2: ObjVertex,
      colorOpt: Option[Float4d],
      vertices: mutable.Buffer[Float4d],
      textureOpts: mutable.Buffer[Option[Float3d]],
      normalOpts: mutable.Buffer[Option[Float3d]],
      colorOpts: mutable.Buffer[Option[Float4d]]
  ): Unit =
    addVertexTextureNormal(objObject, vtn0, colorOpt, vertices, textureOpts, normalOpts, colorOpts)
    addVertexTextureNormal(objObject, vtn1, colorOpt, vertices, textureOpts, normalOpts, colorOpts)
    addVertexTextureNormal(objObject, vtn2, colorOpt, vertices, textureOpts, normalOpts, colorOpts)

  private def addVertexTextureNormal(
      objObject: ObjObject,
      objVertex: ObjVertex,
      colorOpt: Option[Float4d],
      vertices: mutable.Buffer[Float4d],
      textureOpts: mutable.Buffer[Option[Float3d]],
      normalOpts: mutable.Buffer[Option[Float3d]],
      colorOpts: mutable.Buffer[Option[Float4d]]
  ): Unit =
    val (vertexIdx, textureIdxOpt, normalIdxOpt) = objVertex
    vertices.addOne(objObject.vertices(vertexIdx - 1))
    textureOpts.addOne(textureIdxOpt.map(textureIdx => objObject.textures(textureIdx - 1)))
    normalOpts.addOne(normalIdxOpt.map(normalIdx => objObject.normals(normalIdx - 1)))
    colorOpts.addOne(colorOpt)

  private def prepareVertices(gl: WebGLRenderingContext, program: WebGLProgram, vertices: Vector[Float4d]): Int =
    val vertexArray = Float32Array(js.Array(vertices.flatMap(_.toList)*))
    VertexBufferObject.initializeVbo(gl, vertexArray)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Position",
      size = 4,
      stride = 0,
      offset = 0
    )

  private def prepareTextures(gl: WebGLRenderingContext, program: WebGLProgram, textures: Vector[Float3d]): Int =
    val texArray = Float32Array(js.Array(textures.flatMap(_.toList)*))
    VertexBufferObject.initializeVbo(gl, texArray)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_TexCoords",
      size = 3,
      stride = 0,
      offset = 0
    )

  private def prepareNormals(
      gl: WebGLRenderingContext,
      program: WebGLProgram,
      normalsOpt: Option[Vector[Float3d]],
      vertices: Vector[Float4d]
  ): Int =
    val normals = normalsOpt match
      case None              =>
        vertices.grouped(3).toVector.flatMap:
          case Vector((x0, y0, z0, w0), (x1, y1, z1, w1), (x2, y2, z2, w2)) =>
            val v0     = Vec4(x0, y0, z0, w0)
            val v1     = Vec4(x1, y1, z1, w1)
            val v2     = Vec4(x2, y2, z2, w2)
            val normal = (v1 - v0).cross(v2 - v0).normal
            Vector(normal(0), normal(1), normal(2), normal(0), normal(1), normal(2), normal(0), normal(1), normal(2))
          case unexpected                                                   =>
            console.log(s"Expected a Vector of three 4D tuples, found $unexpected.")
            Vector.empty
      case Some(floatVector) => floatVector.flatMap(_.toList.toVector)
    VertexBufferObject.initializeVbo(gl, Float32Array(js.Array(normals*)))
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Normal",
      size = 3,
      stride = 0,
      offset = 0
    )

  private def prepareColors(gl: WebGLRenderingContext, program: WebGLProgram, colors: Vector[Float4d]): Int =
    val colorArray = Float32Array(js.Array(colors.flatMap(_.toList)*))
    VertexBufferObject.initializeVbo(gl, colorArray)
    WebglAttribute.enableAttribute(
      gl,
      program,
      WebGLRenderingContext.FLOAT,
      "a_Color",
      size = 4,
      stride = 0,
      offset = 0
    )

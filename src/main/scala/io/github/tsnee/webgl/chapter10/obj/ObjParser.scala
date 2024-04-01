package io.github.tsnee.webgl.chapter10.obj

import cats.syntax.all._
import io.github.tsnee.webgl.chapter10.FileDownloader
import io.github.tsnee.webgl.chapter10.obj.types._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.parsing.combinator._

object ObjParser extends RegexParsers with CommonParserRules:
  override def skipWhitespace: Boolean = true

  override val whiteSpace: Regex = """ +""".r

  def parseObj(filename: String): Future[Either[String, ObjDoc]] =
    FileDownloader.downloadFileFuture(filename, downloadCallback)

  private def downloadCallback(fileContents: String): Future[Either[String, ObjDoc]] = parse(obj, fileContents) match
    case Success(objDoc, remaining) => objDoc
    case Failure(t, remaining)      => Future.successful(s"Failure! $t with remaining text '$remaining'.".asLeft)
    case Error(t, remaining)        => Future.successful(s"Error! $t with remaining text '$remaining'.".asLeft)

  private def obj: Parser[Future[Either[String, ObjDoc]]]                      = comments ~ mtls ~ objObjects ^^ {
    case _ ~ futureM ~ o => futureM.map(_.map(ObjDoc(_, o)))
  }
  private def mtls: Parser[Future[Either[String, Map[String, MtlDoc]]]]        = rep1(mtl) ^^ {
    listOfFuture =>
      listOfFuture.sequence.map(listOfEither =>
        listOfEither.sequence.map(listOfMtlDoc => listOfMtlDoc.map(mtlDoc => mtlDoc.name -> mtlDoc).toMap)
      )
  }
  private def mtl: Parser[Future[Either[String, MtlDoc]]]                      = "mtllib" ~ name ~ EOL ^^ {
    case "mtllib" ~ n ~ _ => MtlParser.parseMtl(n)
  }
  private def objObjects: Parser[Map[String, ObjObject]]                       = rep1(objObject) ^^ {
    _.map(obj => obj.name -> obj).toMap
  }
  private def objObject: Parser[ObjObject]                                     =
    ("o" | "g") ~ name ~ EOL ~ rep1(vertex) ~ rep(vertexTexture) ~ rep(vertexNormal) ~ rep(materialGroup) ^^ {
      case _ ~ n ~ _ ~ vs ~ vts ~ vns ~ ms => ObjObject(n, vs.toVector, vts.toVector, vns.toVector, ms.toMap)
    }
  private def vertex: Parser[Float4d]                                          = vertex3 | vertex4
  private def vertex3: Parser[Float4d]                                         = "v" ~ float ~ float ~ float ~ EOL ^^ {
    case "v" ~ v0 ~ v1 ~ v2 ~ _ => (v0, v1, v2, 1)
  }
  private def vertex4: Parser[Float4d]                                         = "v" ~ float ~ float ~ float ~ float ~ EOL ^^ {
    case "v" ~ v0 ~ v1 ~ v2 ~ v3 ~ _ => (v0, v1, v2, v3)
  }
  private def vertexTexture: Parser[Float3d]                                   = "vt" ~ float ~ float ~ float ~ EOL ^^ {
    case "vt" ~ v0 ~ v1 ~ v2 ~ _ => (v0, v1, v2)
  }
  private def vertexNormal: Parser[Float3d]                                    = "vn" ~ float ~ float ~ float ~ EOL ^^ {
    case "vn" ~ v0 ~ v1 ~ v2 ~ _ => (v0, v1, v2)
  }
  private def materialGroup: Parser[(String, Vector[Face])]                    = "usemtl" ~ name ~ EOL ~ rep1(face) ^^ {
    case _ ~ name ~ _ ~ faces => name -> faces.toVector
  }
  private def face: Parser[Face]                                               = "f" ~ faceElements ~ EOL ^^ {
    case _ ~ elements ~ _ => Face(elements.toVector)
  }
  private def faceElements: Parser[List[(Int, Option[Int], Option[Int])]]      =
    rep1(vertexAndTextureAndNormalIndices) | rep1(vertexAndNormalIndices) | rep1(vertexAndTextureIndices) | rep1(
      vertexIndex
    )
  private def vertexAndTextureAndNormalIndices                                 = int ~ "/" ~ int ~ "/" ~ int ^^ {
    case v ~ "/" ~ t ~ "/" ~ n => (v, t.some, n.some)
  }
  private def vertexAndNormalIndices: Parser[(Int, Option[Int], Option[Int])]  = int ~ "//" ~ int ^^ {
    case v ~ "//" ~ n => (v, None, n.some)
  }
  private def vertexAndTextureIndices: Parser[(Int, Option[Int], Option[Int])] = int ~ "/" ~ int ^^ {
    case v ~ "/" ~ t => (v, t.some, None)
  }
  private def vertexIndex: Parser[(Int, Option[Int], Option[Int])]             = int ^^ {
    (_, None, None)
  }

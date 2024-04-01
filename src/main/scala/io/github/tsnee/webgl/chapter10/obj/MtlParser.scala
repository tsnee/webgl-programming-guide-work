package io.github.tsnee.webgl.chapter10.obj

import cats.syntax.all._
import io.github.tsnee.webgl.chapter10.FileDownloader
import io.github.tsnee.webgl.chapter10.obj.types._

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object MtlParser extends RegexParsers with CommonParserRules:
  override def skipWhitespace: Boolean = true

  override val whiteSpace: Regex = """ +""".r

  def parseMtl(filename: String): Future[Either[String, MtlDoc]] =
    FileDownloader.downloadFile(filename, downloadCallback(filename))

  private def downloadCallback(filename: String)(response: String): Either[String, MtlDoc] =
    parse(mtlDoc(filename), response) match
      case Success(md, _)        => md.asRight
      case Failure(t, remaining) => s"MtlParser Failure! $t with remaining text '$remaining'.".asLeft
      case Error(t, remaining)   => s"MtlParser Error! $t with remaining text '$remaining'.".asLeft

  private def mtlDoc(name: String): Parser[MtlDoc] = comments ~ rep1(material) ^^ {
    case _ ~ mtls => MtlDoc(name, mtls.map(mtl => mtl.name -> mtl).toMap)
  }
  private def material: Parser[Material]           =
    "newmtl" ~ name ~ EOL ~ ka ~ kd ~ ks ~ ns ~ ni ~ d ~ illum ^^ {
      case _ ~ mtlName ~ _ ~ _ ~ (r, g, b) ~ _ ~ _ ~ _ ~ _ ~ _ => Material(mtlName, r, g, b)
    }
  private def ka: Parser[Float3d]                  = "Ka" ~ float ~ float ~ float ~ EOL ^^ {
    case "Ka" ~ r ~ g ~ b ~ _ => (r, g, b)
  }
  private def kd: Parser[Float3d]                  = "Kd" ~ float ~ float ~ float ~ EOL ^^ {
    case "Kd" ~ r ~ g ~ b ~ _ => (r, g, b)
  }
  private def ks: Parser[Float3d]                  = "Ks" ~ float ~ float ~ float ~ EOL ^^ {
    case "Ks" ~ r ~ g ~ b ~ _ => (r, g, b)
  }
  private def ns: Parser[Float]                    = "Ns" ~ float ~ EOL ^^ {
    case "Ns" ~ fl ~ _ => fl.toFloat
  }
  private def ni: Parser[Float]                    = "Ni" ~ float ~ EOL ^^ {
    case "Ni" ~ fl ~ _ => fl.toFloat
  }
  private def d: Parser[Float]                     = "d" ~ float ~ EOL ^^ {
    case "d" ~ fl ~ _ => fl.toFloat
  }
  private def illum: Parser[Int]                   = "illum" ~ int ~ EOL ^^ {
    case "illum" ~ i ~ _ => i.toInt
  }

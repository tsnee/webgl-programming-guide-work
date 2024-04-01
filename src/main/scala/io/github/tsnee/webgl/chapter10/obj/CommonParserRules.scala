package io.github.tsnee.webgl.chapter10.obj

import scala.util.parsing.combinator._

trait CommonParserRules:
  self: RegexParsers =>
  def comments: Parser[List[String]]  = rep(comment)
  private def comment: Parser[String] = """#.*""".r ~ EOL ^^ { case comment ~ _ => comment }
  def float: Parser[Float]            = """[+-]?\d+\.\d+""".r ^^ { _.toFloat }
  def int: Parser[Int]                = """[+-]?\d+""".r ^^ { _.toInt }
  def name: Parser[String]            = """[\w.]+""".r
  def EOL: Parser[String]             = nl | crlf
  private def nl: Parser[String]      = "\n"
  private def crlf: Parser[String]    = "\r\n"

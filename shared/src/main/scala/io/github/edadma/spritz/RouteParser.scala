package io.github.edadma.spritz

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.{PagedSeq, PagedSeqReader}

object RouteParser extends RegexParsers:

  override val skipWhitespace = false

  def route: Parser[RouteAST] =
    segment ~ rep(segment) ^^ { case first ~ rest =>
      rest.foldLeft(first) { case (l, r) => RouteAST.Sequence(l, r) }
    }

  def segment: Parser[RouteAST] =
    "/" ~ piece ~ rep(piece) ^^ { case _ ~ p ~ ps =>
      RouteAST.Sequence(RouteAST.Slash, ps.foldLeft(p) { case (l, r) => RouteAST.Sequence(l, r) })
    }

  def piece: Parser[RouteAST] = parameter | literal

  def literal: Parser[RouteAST] = "[a-zA-Z0-9-_.]+".r ^^ RouteAST.Literal.apply

  def parameter: Parser[RouteAST] = ":[a-zA-Z0-9_]+".r ^^ (n => RouteAST.Parameter(n drop 1))

  def apply(input: scala.io.Source): RouteAST =
    parseAll(route, new PagedSeqReader(PagedSeq.fromSource(input))) match {
      case Success(result, _) => result
      case Error(msg, next)   => sys.error(s"$msg: ${next.pos.longString}")
      case Failure(msg, next) => sys.error(s"$msg: ${next.pos.longString}")
    }

  def apply(input: String): RouteAST = apply(scala.io.Source.fromString(input))

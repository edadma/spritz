package io.github.edadma.spritz

enum RouteAST:
  case Slash extends RouteAST
  case Literal(segment: String) extends RouteAST
  case Parameter(name: String) extends RouteAST
  case Sequence(elems: Seq[RouteAST]) extends RouteAST

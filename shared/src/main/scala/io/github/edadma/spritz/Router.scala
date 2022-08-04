package io.github.edadma.spritz

import io.github.edadma.spritz.RouteAST.Slash

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class Router extends Handler:

  private[spritz] val routes = new ListBuffer[Route]

  private def regex(route: String): Regex =
    val buf = new mutable.StringBuilder

    def regex(elem: RouteAST): Unit =
      elem match
        case Slash                     => buf += '/'
        case RouteAST.Literal(segment) => buf ++= segment
        case RouteAST.Parameter(name) =>
          buf += ':'
          buf ++= s"(?<$name>[^\\/#\\?]+?)"
        case RouteAST.Sequence(elems) => elems foreach regex

    buf += '^'
    regex(RouteParser(route))
    buf.toString.r

  def get(route: String, handler: Handler): Router =
    routes += Route.Request("GET", regex(route), handler)
    this

  def apply(req: Request): Unit =
    for Route.Request(method, path, handler) <- routes do
      if method == req.method then
        path.findFirstMatchIn(req.rest) match
          case Some(m) if m.end == req.rest.length =>
            handler(req)
            return
          case _ =>

    sys.error(s"no matching route for ${req.rest}")

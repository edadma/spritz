package io.github.edadma.spritz

import io.github.edadma.spritz.RouteAST.Slash

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class Router extends Handler:

  private[spritz] val routes = new ListBuffer[Route]

  private def regex(route: String): (Regex, Seq[String]) =
    val buf = new mutable.StringBuilder
    val groups = new ListBuffer[String]

    def regex(elem: RouteAST): Unit =
      elem match
        case Slash                     => buf ++= "/?"
        case RouteAST.Literal(segment) => buf ++= segment
        case RouteAST.Parameter(name) =>
          buf ++= s"(?<$name>[^/#\\?]+)"
          groups += name
        case RouteAST.Sequence(elems) => elems foreach regex

    buf += '^'
    regex(RouteParser(route))
    (buf.toString.r, groups.toSeq)

  def get(route: String, handler: Handler): Router =
    val (path, params) = regex(route)

    routes += Route.Endpoint("GET", path, params, handler)
    this

  protected def callHandler(h: Handler, req: Request)
  def apply(req: Request): Unit =
    for route <- routes do
      route match
        case Route.Endpoint(method, path, params, handler) =>
          if method == req.method then
            path.findPrefixMatchOf(req.rest) match
              case Some(m) if m.end == req.rest.length =>
                handler(
                  req.copy(rest = req.rest.substring(m.end), params = req.params ++ (params map (k => k -> m.group(k)))),
                )
                return
              case Some(m) =>
              case None    =>
        case Route.Middleware(handler) =>
    end for

    sys.error(s"no matching route for ${req.rest}")

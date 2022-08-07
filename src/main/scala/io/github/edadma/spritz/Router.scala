package io.github.edadma.spritz

import io.github.edadma.spritz.RouteAST.Slash

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

class Router extends RequestHandler:

  private[spritz] val routes = new ListBuffer[Route]

  private def regex(path: String): (Regex, Seq[String]) =
    val buf = new mutable.StringBuilder
    val groups = new ListBuffer[String]

    def regex(elem: RouteAST): Unit =
      elem match
        case Slash                     => buf ++= "/"
        case RouteAST.Literal(segment) => buf ++= segment
        case RouteAST.Parameter(name) =>
          buf ++= s"(?<$name>[^/#\\?]+)"
          groups += name
        case RouteAST.Sequence(elems) => elems foreach regex

    buf += '^'

    RouteParser(path) match
      case Slash => buf ++= "/?"
      case ast   => regex(ast)

    (buf.toString.r, groups.toSeq)

  protected def endpoint(method: Method, path: String, handler: EndpointHandler): Router =
    val (pathr, params) = regex(path)

    routes += Route.Endpoint(method, pathr, params, handler)
    this

  def get(path: String, handler: EndpointHandler): Router = endpoint("GET", path, handler)

  def post(path: String, handler: EndpointHandler): Router = endpoint("POST", path, handler)

  def put(path: String, handler: EndpointHandler): Router = endpoint("PUT", path, handler)

  def delete(path: String, handler: EndpointHandler): Router = endpoint("DELETE", path, handler)

  def patch(path: String, handler: EndpointHandler): Router = endpoint("PATCH", path, handler)

  def use(path: String, middleware: RequestHandler): Router =
    val (pathr, params) = regex(path)

    routes += Route.PathRoutes(pathr, params, middleware)
    this

  def use(middleware: RequestHandler): Router =
    routes += Route.Middleware(middleware)
    this

  def apply(req: Request, res: Response): HandlerResult =
    for route <- routes do
      route match
        case Route.Endpoint(method, path, params, handler) =>
          if method == req.method then
            path.findPrefixMatchOf(req.rest) match
              case Some(m) if m.end == req.rest.length =>
                params foreach (k => req.params(k) = m.group(k))
                req.route ++= req.rest.substring(0, m.end)
                req.rest = req.rest.substring(m.end)
                handler(req, res)
                return HandlerResult.Done
              case _ =>
        case Route.PathRoutes(path, params, handler) =>
          path.findPrefixMatchOf(req.rest) match
            case Some(m) =>
              params foreach (k => req.params(k) = m.group(k))
              req.route ++= req.rest.substring(0, m.end)
              req.rest = req.rest.substring(m.end)
              handler(req, res) match
                case HandlerResult.Done       => return HandlerResult.Done
                case HandlerResult.Next       =>
                case HandlerResult.Error(err) => return HandlerResult.Error(err)
            case _ =>
        case Route.Middleware(handler) =>
          handler(req, res) match
            case HandlerResult.Done       => return HandlerResult.Done
            case HandlerResult.Next       =>
            case HandlerResult.Error(err) => return HandlerResult.Error(err)
    end for

    HandlerResult.Next

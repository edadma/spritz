package io.github.edadma.spritz

import io.github.edadma.spritz.RouteAST.Slash

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future
import scala.util.matching.Regex

import Server.eventLoop

class Router extends MiddlewareHandler:

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
    endpointAsync(method, path, (req, res) => Future(handler(req, res)))

  protected def endpointAsync(method: Method, path: String, handler: AsyncEndpointHandler): Router =
    val (pathr, params) = regex(path)

    routes += Route.EndpointAsync(method, pathr, params, (req, res) => Future(handler(req, res)))
    this

  def get(path: String, handler: AsyncEndpointHandler): Router = endpointAsync("GET", path, handler)

//  def getAsync(path: String, handler: AsyncEndpointHandler): Router = endpointAsync("GET", path, handler)

  def post(path: String, handler: AsyncEndpointHandler): Router = endpointAsync("POST", path, handler)

  def put(path: String, handler: AsyncEndpointHandler): Router = endpointAsync("PUT", path, handler)

  def delete(path: String, handler: AsyncEndpointHandler): Router = endpointAsync("DELETE", path, handler)

  def patch(path: String, handler: AsyncEndpointHandler): Router = endpointAsync("PATCH", path, handler)

  def use(path: String, middleware: MiddlewareHandler): Router =
    val (pathr, params) = regex(path)

    routes += Route.PathRoutes(pathr, params, middleware)
    this

  def use(middleware: MiddlewareHandler): Router =
    routes += Route.Middleware(middleware)
    this

  protected def routeMatch(req: Request, params: Seq[String], m: Regex.Match): Unit =
    params foreach (k => req.params(k) = Util.urlDecode(m.group(k)))
    req.route ++= req.rest.substring(0, m.end)
    req.rest = req.rest.substring(m.end)

  def apply(req: Request, res: Response): HandlerResult =
    for route <- routes do
      route match
        case Route.EndpointAsync(method, path, params, handler) =>
          if method == req.method then
            path.findPrefixMatchOf(req.rest) match
              case Some(m) if m.end == req.rest.length =>
                routeMatch(req, params, m)
                return HandlerResult.Found(handler(req, res))
              case _ =>
        case Route.PathRoutes(path, params, handler) =>
          path.findPrefixMatchOf(req.rest) match
            case Some(m) =>
              routeMatch(req, params, m)
              handler(req, res) match
                case f: HandlerResult.Found   => return f
                case HandlerResult.Next       =>
                case HandlerResult.Error(err) => return HandlerResult.Error(err)
            case _ =>
        case Route.Middleware(handler) =>
          handler(req, res) match
            case f: HandlerResult.Found   => return f
            case HandlerResult.Next       =>
            case HandlerResult.Error(err) => return HandlerResult.Error(err)
    end for

    HandlerResult.Next

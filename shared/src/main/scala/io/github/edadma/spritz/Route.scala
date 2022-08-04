package io.github.edadma.spritz

import scala.util.matching.Regex

type EndpointHandler = (Request, Response) => Unit

type RequestHandler = (Request, Response) => HandlerResult

type Method = "GET" | "POST"

enum Route:
  case Endpoint(method: Method, path: Regex, params: Seq[String], handler: EndpointHandler) extends Route
  case PathRoutes(path: Regex, params: Seq[String], handler: RequestHandler) extends Route
  case Middleware(handler: RequestHandler) extends Route

enum HandlerResult:
  case Done extends HandlerResult
  case Next extends HandlerResult
  case Error(err: Any) extends HandlerResult

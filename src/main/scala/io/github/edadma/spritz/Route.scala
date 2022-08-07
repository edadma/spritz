package io.github.edadma.spritz

import scala.concurrent.Future
import scala.util.matching.Regex

type EndpointHandler = (Request, Response) => Future[Unit]

type MiddlewareHandler = (Request, Response) => HandlerResult

type Method = "GET" | "POST" | "PUT" | "DELETE" | "PATCH"

enum Route:
  case Endpoint(method: Method, path: Regex, params: Seq[String], handler: EndpointHandler) extends Route
  case PathRoutes(path: Regex, params: Seq[String], handler: MiddlewareHandler) extends Route
  case Middleware(handler: MiddlewareHandler) extends Route

enum HandlerResult:
  case Found(future: Future[Unit]) extends HandlerResult
  case Next extends HandlerResult
  case Error(err: Any) extends HandlerResult

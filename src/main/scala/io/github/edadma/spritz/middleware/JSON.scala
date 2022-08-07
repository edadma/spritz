package io.github.edadma.spritz.middleware

import io.github.edadma.spritz.{HandlerResult, Request, RequestHandler, Response}

object JSON extends RequestHandler:
  def apply(req: Request, res: Response): HandlerResult =
    req.headers get "content-type" match
      case Some("application/json") =>

      case _ =>

    HandlerResult.Next
package io.github.edadma.spritz.middleware

import io.github.edadma.json.DefaultJSONReader
import io.github.edadma.spritz.{HandlerResult, Request, RequestHandler, Response}

import scala.io.Codec

object JSON extends RequestHandler:
  def apply(req: Request, res: Response): HandlerResult =
    println(("JSON", req))
    req.headers get "content-type" match
      case Some("application/json") =>
        req.body = DefaultJSONReader.fromString(new String(Codec.fromUTF8(req.payload))).asInstanceOf[Map[String, Any]]
        println(req.body)
      case _ =>

    HandlerResult.Next

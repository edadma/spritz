package io.github.edadma.spritz.middleware

import scala.collection.mutable
import io.github.edadma.json.DefaultJSONReader
import io.github.edadma.spritz.{DMap, HandlerResult, Request, MiddlewareHandler, Response}

import scala.io.Codec

object JSON extends MiddlewareHandler:
  def apply(req: Request, res: Response): HandlerResult =
    req.headers get "content-type" match
      case Some("application/json") =>
        req.body = new DMap(
          new mutable.HashMap().addAll(
            DefaultJSONReader.fromString(new String(Codec.fromUTF8(req.payload))).asInstanceOf[Map[String, Any]],
          ),
        )
      case _ =>

    HandlerResult.Next

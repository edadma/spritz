package io.github.edadma.spritz

import scala.collection.mutable

class Request(
    var method: Method,
    var path: String,
    val headers: mutable.Map[String, String],
    val params: mutable.Map[String, String],
    val payload: Array[Byte],
):
  var body: Map[String, Any] = null
  var route: String = ""
  var rest: String = path

  override def toString: String = s"$method $path headers=[${headers.mkString(", ")}] params=[${params.mkString(", ")}]"

package io.github.edadma.spritz

class Request(
    var method: Method,
    var path: String,
    var headers: Map[String, String],
    params: Map[String, String],
    val payload: Seq[Byte],
):
  var body: Map[String, Any] = null
  var route: String = ""
  var rest: String = path

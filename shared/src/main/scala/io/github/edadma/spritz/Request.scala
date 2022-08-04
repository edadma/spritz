package io.github.edadma.spritz

case class Request(
    method: Method,
    path: String,
    headers: Seq[(String, String)],
    params: Map[String, String],
    rest: String,
)

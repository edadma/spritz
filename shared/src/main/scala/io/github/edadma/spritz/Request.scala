package io.github.edadma.spritz

case class Request(
    method: Method,
    path: String,
    headers: Map[String, String],
    params: Map[String, String],
    route: String,
    rest: String,
)

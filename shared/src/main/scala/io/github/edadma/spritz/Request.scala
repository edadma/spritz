package io.github.edadma.spritz

case class Request(method: Method, path: String, params: Map[String, String], rest: String)

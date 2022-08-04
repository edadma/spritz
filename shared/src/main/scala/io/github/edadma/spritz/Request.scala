package io.github.edadma.spritz

case class Request(method: Method, params: Map[String, String], rest: String)

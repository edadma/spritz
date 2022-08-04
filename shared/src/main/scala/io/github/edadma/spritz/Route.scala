package io.github.edadma.spritz

import scala.util.matching.Regex

type Handler = Request => Unit

type Method = "GET" | "POST"

enum Route:
  case Request(method: Method, path: Regex, params: Seq[String], handler: io.github.edadma.spritz.Handler) extends Route
  case Handler(handler: io.github.edadma.spritz.Handler) extends Route

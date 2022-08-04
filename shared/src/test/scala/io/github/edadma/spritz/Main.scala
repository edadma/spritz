package io.github.edadma.spritz

import pprint.pprintln

@main def run(): Unit =
  val router =
    new Router().get("/asdf", req => println(req))

  router(Request("GET", Map(), "/asdf"))

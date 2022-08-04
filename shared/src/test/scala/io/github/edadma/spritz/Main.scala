package io.github.edadma.spritz

import pprint.pprintln

@main def run(): Unit =
  val birds =
    Router().get("/", req => println(("/birds", req))).get("/:id", req => println(("/birds/:id", req)))

  new Server {
    def main = { app =>
      app.get("/birds", birds)
    }
  }

abstract class Server:
  def main: Router => Unit

  private val router = Router()

  main(router)
  println("listening")

  router(Request("GET", "/birds/asdf", Map(), "/birds/asdf"))

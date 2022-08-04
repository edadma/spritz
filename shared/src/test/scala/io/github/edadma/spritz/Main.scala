package io.github.edadma.spritz

import pprint.pprintln

@main def run(): Unit =
  val birds =
    Router()
      .get("/", req => println(("=> /birds", req)))
      .get("/:id", req => println(("=> /birds/:id", req)))

  new Server {
    def main = { app =>
      app.use("/birds", birds)
    }
  }

abstract class Server:
  def main: Router => Unit

  protected val router = new Router

  main(router)

  router.use { req =>
    println("nothing matched")
    HandlerResult.Done
  }

  println("listening")

  println(router(Request("GET", "/birds/asdf", Map(), "/birds/asdf")))

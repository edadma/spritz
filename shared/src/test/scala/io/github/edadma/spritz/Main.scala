package io.github.edadma.spritz

import pprint.pprintln

@main def run(): Unit =
  val birds =
    Router()
      .get("/", (req, res) => println(("=> /birds", req)))
      .get("/:id", (req, res) => println(("=> /birds/:id", req)))

  new Server {
    def main = { app =>
      app.use("/birds", birds)
    }
  }

abstract class Server:
  def main: Router => Unit

  protected val router = new Router

  main(router)

  router.use { (req, res) =>
    res.status(500).send(s"no matching routes for path '${req.path}'")
    HandlerResult.Done
  }

  println("listening")

  process(Request("GET", "/birds/asdf", Seq(), Map(), "/birds/asdf"))

  def process(req: Request): Unit =
    val res = new Response

    router(req, res)
    println(res)

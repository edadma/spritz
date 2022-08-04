package io.github.edadma.spritz

abstract class Server(serverName: String):
  def main: Router => Unit

  protected val router = new Router

  main(router)

  router.use { (req, res) =>
    res.status(500).send(s"no matching routes for path '${req.path}'")
    HandlerResult.Done
  }

  println("listening")

  def process(req: Request): Unit =
    val res = new Response(serverName)

    router(req, res)
    println(res)

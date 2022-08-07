package io.github.edadma.spritz

@main def run(): Unit =
//  val birds =
//    Router()
//      .get("/", (req, res) => res.send(("get /birds", req.headers("accept"))))
//      .get("/:id", (req, res) => res.send(("get /birds/:id", req)))
//      .post("/:id", (req, res) => res.send(("post /birds/:id", req)))
//
//  Server { app =>
//    app.use(middleware.JSON)
//    app.use("/birds", birds)
//    app.listen(8000, "ETA_SERVER/0.0.1")
//    println("listening")
//  }

  import io.github.edadma.spritz.libuv._
  import io.github.edadma.spritz.libuvConstants._

  val loop: Loop = uv_default_loop()

  uv_run(loop, UV_RUN_DEFAULT)

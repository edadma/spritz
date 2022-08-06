package io.github.edadma.spritz

@main def run(): Unit =
  val birds =
    Router()
      .get("/", (req, res) => res.send("<p>=> /birds</p>"))
      .get("/:id", (req, res) => res.send(("=> /birds/:id", req)))

  Server { app =>
    app.use("/birds", birds)
    app.listen(8000, "ETA_SERVER/0.0.1")
    println("listening")
  }

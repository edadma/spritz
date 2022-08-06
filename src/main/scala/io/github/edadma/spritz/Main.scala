package io.github.edadma.spritz

import pprint.pprintln

import scala.language.postfixOps

@main def run(): Unit =
  val birds =
    Router()
      .get("/", (req, res) => res.send("<p>=> /birds</p>"))
      .get("/:id", (req, res) => res.send(("=> /birds/:id", req)))

  val app = Spritz("ETA_SERVER/0.0.1")

  app.use("/birds", birds)
  app.listen(8000)

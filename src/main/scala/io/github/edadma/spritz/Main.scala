package io.github.edadma.spritz

import pprint.pprintln

import scala.language.postfixOps

@main def run(): Unit =
  val birds =
    Router()
      .get("/", (req, res) => res.send("<p>=> /birds</p>"))
      .get("/:id", (req, res) => res.send(("=> /birds/:id", req)))

  new Server("0.0.0.0", 8000, 0, 4096, "ETA_SERVER/0.0.1") {
    def main = { app =>
      app.use("/birds", birds)
    }
  }

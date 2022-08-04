package io.github.edadma.spritz

import pprint.pprintln

@main def run(): Unit =
  val birds =
    Router()
      .get("/", (req, res) => println(("=> /birds", req)))
      .get("/:id", (req, res) => println(("=> /birds/:id", req)))

  new Server("ETA_SERVER/0.0.1") {
    def main = { app =>
      app.use("/birds", birds)
    }
  }

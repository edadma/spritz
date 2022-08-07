package io.github.edadma.spritz

import scala.concurrent.Future

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

  println("hello")

  implicit val loop: EventLoop.type = EventLoop

  println("setting up futures")

  Future {
    println("Future 1!")
  }.map { _ =>
    println("Future 2!")
  }

  println("main about to return...")

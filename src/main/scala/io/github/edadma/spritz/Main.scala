package io.github.edadma.spritz

import scala.concurrent.Future
import scala.concurrent.duration.*
//import cps.*
//import cps.monads.FutureAsyncMonad
//import Server.eventLoop

@main def run(): Unit =
//
//  def get(req: Request, res: Response) = async {
//    res.send(("get /birds", req.headers("accept")))
//  }
//
//  val birds =
//    Router()
//      .get("/", get)
//      .get("/:id", (req, res) => async { res.send(("get /birds/:id", req.params.id)) })
//      .post("/:id", (req, res) => async { res.send(("post /birds/:id", req)) })
//
//  Server { app =>
//    app.use(middleware.JSON)
//    app.use("/birds", birds)
//    app.listen(8000, "ETA_SERVER/0.0.1")
//    println("listening")
//  }

//  async {
//    println("hello")
//
//    for i <- 1 to 3 do await { f(i) }
//
//    println("done")
//  }
//
//  def f(i: Int) = async {
//    println(i)
//    await { Timer(1 second) }
//  }

  ThreadPool.schedule(println(123))

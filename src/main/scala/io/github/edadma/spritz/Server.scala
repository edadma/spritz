package io.github.edadma.spritz

import pprint.pprintln

abstract class Server(val serverName: String):
  def main: Router => Unit

  protected val router = new Router

  main(router)

  router.use { (req, res) =>
    res.status(500).send(s"no matching routes for path '${req.path}'")
    HandlerResult.Done
  }

  /////////////

  println("listening")

  val HTTP_request: Array[Byte] = "GET /birds/asdf HTTP/1.1\r\nHost: zxcv.com\r\n\r\n".getBytes

  process(HTTP_request)

  ////////////

  def process(httpreq: Array[Byte]): Unit =
    RequestParser run httpreq
    println(RequestParser.elems)
    pprintln(RequestParser.body)

    val res = new Response(serverName)

    if RequestParser.elems.length < 3 || (RequestParser.elems.length - 3) % 2 != 0 then res.sendStatus(400)
    else
      pprintln(RequestParser.elems drop 2)

      val req =
        Request(
          RequestParser.elems.head.asInstanceOf[Method],
          RequestParser.elems(1),
          RequestParser.elems drop 3 grouped 2 map (c => (c.head, c.tail.head)) toMap,
          Map(),
          "",
          RequestParser.elems(1),
        )

      router(req, res)
      respond(res)
  end process

  def respond(res: Response): Unit =
    println(res)

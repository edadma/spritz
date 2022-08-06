package io.github.edadma.spritz

import pprint.pprintln

import scala.scalanative.libc.stdlib.malloc
import scala.scalanative.unsafe._
import scala.scalanative.unsigned._

abstract class Server(address: String, port: Int, flags: Int, backlog: Int, val serverName: String):
  import io.github.edadma.spritz.libuv._
  import io.github.edadma.spritz.libuvConstants._

  def main: Router => Unit

  protected val router = new Router

  main(router)

  router.use { (req, res) =>
    res.status(500).send(s"no matching routes for path '${req.path}'")
    HandlerResult.Done
  }

  /////////////

  val SOCKADDR_IN = 16
  val socketAddress: Ptr[Byte] = stackalloc[Byte](SOCKADDR_IN.toUInt)

  Zone { implicit z =>
    checkError(uv_ip4_addr(CString.fromString(address), port, socketAddress))
  }

  val server: TCPHandle = malloc(uv_handle_size(UV_TCP_T)).asInstanceOf[TCPHandle]
  val loop: Loop = uv_default_loop()

  checkError(uv_tcp_init(loop, server), "uv_tcp_init(server)")
  checkError(uv_tcp_bind(server, socketAddress, flags), "uv_tcp_bind")
  checkError(uv_listen(server, backlog, callback), "uv_tcp_listen")
  uv_run(loop, UV_RUN_DEFAULT)

  println(s"listening on $address:$port")

  val HTTP_request: Array[Byte] = "GET /birds/asdf HTTP/1.1\r\nHost: zxcv.com\r\n\r\n".getBytes

  process(HTTP_request)

  ////////////

  def checkError(v: Int, label: String): Unit =
    if v != 0 then
      val error = fromCString(uv_err_name(v))
      val message = fromCString(uv_strerror(v))

      sys.error(s"$label returned $v: $error: $message")

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

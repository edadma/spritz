package io.github.edadma.spritz

import pprint.pprintln

import scala.collection.mutable
import scala.scalanative.libc.stdlib.*
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

abstract class Server(address: String, port: Int, flags: Int, backlog: Int, val serverName: String):
  import io.github.edadma.spritz.libuv._
  import io.github.edadma.spritz.libuvConstants._

  def main: Router => Unit

  protected val router = new Router

  protected class Connection(buf: Ptr[Buffter], client: TCPHandle) {

  }

  protected val connectionsMap = new mutable.HashMap[TCPHandle, Connection]

  main(router)

  router.use { (req, res) =>
    res.status(500).send(s"no matching routes for path '${req.path}'")
    HandlerResult.Done
  }

  val SOCKADDR_IN = 16
  val socketAddress: Ptr[Byte] = stackalloc[Byte](SOCKADDR_IN.toUInt)

  Zone { implicit z =>
    checkError(uv_ip4_addr(toCString(address), port, socketAddress), "uv_ip4_addr")
  }

  val server: TCPHandle = malloc(uv_handle_size(UV_TCP_T)).asInstanceOf[TCPHandle]
  val loop: Loop = uv_default_loop()

  checkError(uv_tcp_init(loop, server), "uv_tcp_init(server)")
  checkError(uv_tcp_bind(server, socketAddress, flags), "uv_tcp_bind")

  val ALLOC_SIZE = 1024

  val allocateCB: uv_alloc_cb =
    (client: TCPHandle, size: CSize, buffer: Ptr[Buffer]) =>
      buffer._1 = malloc(ALLOC_SIZE.toUInt)
      buffer._2 = ALLOC_SIZE.toUInt

  def shutdown(client: TCPHandle): Unit = {
    val shutdown_req = malloc(uv_req_size(UV_SHUTDOWN_REQ_T))
      .asInstanceOf[ShutdownReq]
    !shutdown_req = client.asInstanceOf[Ptr[Byte]]
    checkError(uv_shutdown(shutdown_req, client, shutdownCB), "uv_shutdown")
  }

  val shutdownCB: ShutdownCB =
    (shutdownReq: ShutdownReq, status: Int) => {
      println("all pending writes complete, closing TCP connection")
      val client = (!shutdownReq).asInstanceOf[TCPHandle]
      checkError(uv_close(client, closeCB), "uv_close")
      free(shutdownReq.asInstanceOf[Ptr[Byte]])
    }

  val readCB: ReadCB =
    (client: TCPHandle, size: CSSize, buffer: Ptr[Buffer]) =>
      if (size < 0)
        shutdown(client)
      else
        try
          val parsed_request = HTTP.parseRequest(buffer._1, size)
          val response = router(parsed_request)

          println("send_response(client, response)")
          shutdown(client)
        catch
          case e: Throwable =>
            println(s"error during parsing: $e")
            shutdown(client)
  end readCB

  val closeCB: CloseCB =
    (client: TCPHandle) =>
      println("closed client connection")
      connectionsMap -= client
  end closeCB

  val onConnectionCB: uv_connection_cb =
    (handle: TCPHandle, status: Int) =>
      println("received connection")

      // initialize the new client tcp handle and its state
      val client = malloc(uv_handle_size(UV_TCP_T)).asInstanceOf[TCPHandle]

      checkError(uv_tcp_init(loop, client), "uv_tcp_init(client)")
      checkError(uv_accept(handle, client), "uv_accept")
      checkError(uv_read_start(client, allocateCB, readCB), "uv_read_start")
  end onConnectionCB

  checkError(uv_listen(server, backlog, onConnectionCB), "uv_tcp_listen")
  println(s"listening on $address:$port")
  uv_run(loop, UV_RUN_DEFAULT)

  /////////////

//  val HTTP_request: Array[Byte] = "GET /birds/asdf HTTP/1.1\r\nHost: zxcv.com\r\n\r\n".getBytes
//
//  process(HTTP_request)

  ////////////

  def checkError(v: Int, label: String): Unit =
    if v != 0 then
      val error = fromCString(uv_err_name(v))
      val message = fromCString(uv_strerror(v))

      sys.error(s"$label error: $error: $message")

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

package io.github.edadma.spritz

import pprint.pprintln

import scala.collection.mutable
import scala.scalanative.libc.stdlib.*
import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*

object Spritz extends Router:
  import io.github.edadma.spritz.libuv._
  import io.github.edadma.spritz.libuvConstants._

  val SOCKADDR_IN = 16
  val loop: Loop = uv_default_loop()

  var _serverName: Option[String] = None

  def apply(serverName: String = null): Spritz.type =
    if serverName ne null then _serverName = Some(serverName)
    this

  def listen(port: Int, flags: Int = 0, backlog: Int = 4096): Unit =
    use { (req, res) =>
      res.status(500).send(s"no matching routes for path '${req.path}'")
      HandlerResult.Done
    }

    val socketAddress: Ptr[Byte] = stackalloc[Byte](SOCKADDR_IN.toUInt)

    checkError(uv_ip4_addr(c"0.0.0.0", port, socketAddress), "uv_ip4_addr")

    val server: TCPHandle = malloc(uv_handle_size(UV_TCP_T)).asInstanceOf[TCPHandle]

    checkError(uv_tcp_init(loop, server), "uv_tcp_init(server)")
    checkError(uv_tcp_bind(server, socketAddress, flags), "uv_tcp_bind")
    checkError(uv_listen(server, backlog, onConnectionCB), "uv_tcp_listen")
    println(s"listening on port $port")
    uv_run(loop, UV_RUN_DEFAULT)

  protected class Connection(client: TCPHandle, buffer: Ptr[Buffer]) {}

  protected val connectionsMap = new mutable.HashMap[TCPHandle, Connection]

  val ALLOC_SIZE = 1024

  val allocateCB: uv_alloc_cb =
    (client: TCPHandle, size: CSize, buffer: Ptr[Buffer]) =>
      buffer._1 = malloc(ALLOC_SIZE.toUInt)
      buffer._2 = ALLOC_SIZE.toUInt
      connectionsMap(client) = new Connection(client, buffer)

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

  val readCB: uv_read_cb =
    (client: TCPHandle, size: CSSize, buffer: Ptr[Buffer]) =>
      if (size < 0)
        shutdown(client)
      else
        try
//          val parsed_request = HTTP.parseRequest(buffer._1, size)
//          val response = router(parsed_request)

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
      ()
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

    val res = new Response(_serverName)

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

      apply(req, res)
      respond(res)
  end process

  def respond(res: Response): Unit =
    println(res)

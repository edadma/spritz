package io.github.edadma.spritz

import scala.concurrent.Future
import scala.concurrent.duration.*
import cps.*
import cps.monads.FutureAsyncMonad
import Server.eventLoop

import scalanative.unsafe._
import LibUV._
import LibUVConstants._

@main def run(): Unit =

  val handle = stackalloc[Byte](uv_handle_size(UV_PROCESS_T)).asInstanceOf[ProcessHandle]
  val options = stackalloc[ProcessOptions]()
  val args = stackalloc[CArray[CString, Nat._3]]()
  val file = c"sleep"

  val exitcb: ExitCB =
    (handle: ProcessHandle, exit_status: CLong, term_signal: CInt) =>
      println(s"exit status: $exit_status")
      uv_close(handle, null)

  args(0) = file
  args(1) = c"2"
  args(2) = null

  options._1 = exitcb
  options._2 = file
  options._3 = args.asInstanceOf[Ptr[CString]]

  println("start")
  val r = uv_spawn(Server.loop, handle, options)

  println(r)

  async {
    for i <- 1 to 5 do
      println(i)
      await(Timer(500 millis))
  }

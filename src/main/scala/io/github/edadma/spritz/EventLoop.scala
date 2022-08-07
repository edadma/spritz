package io.github.edadma.spritz

import scala.scalanative.unsafe._
import scala.scalanative.libc.stdlib

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.{Try, Success}
import scala.Option

object EventLoop extends ExecutionContextExecutor {
  import libuv._
  import libuvConstants._
  import Util.checkError

  val loop: Loop = uv_default_loop()
  private val taskQueue = new mutable.Queue[Runnable]
  private val handle = stdlib.malloc(uv_handle_size(UV_PREPARE_T))

  checkError(uv_prepare_init(loop, handle), "uv_prepare_init")

  val prepareCallback: PrepareCB =
    (handle: PrepareHandle) =>
      while taskQueue.nonEmpty do
        val runnable = taskQueue.dequeue

        try runnable.run()
        catch case t: Throwable => reportFailure(t)

      if taskQueue.isEmpty then
        println("stopping dispatcher")
        uv_prepare_stop(handle)

  def execute(runnable: Runnable): Unit = {
    taskQueue enqueue runnable
    checkError(uv_prepare_start(handle, prepareCallback), "uv_prepare_start")
  }

  def reportFailure(t: Throwable): Unit = {
    println(s"Future failed with Throwable $t:")
    t.printStackTrace()
  }

  def run(mode: Int = UV_RUN_DEFAULT): Unit = {
    var continue = 1
    while (continue != 0) {
      continue = uv_run(loop, mode)
      println(s"uv_run returned $continue")
    }
  }

  private val bootstrapFuture = Future(run())(ExecutionContext.global)
}

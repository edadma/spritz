package io.github.edadma.spritz

import scala.Option
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}
import scala.scalanative.libc.stdlib._
import scala.scalanative.unsafe.*
import scala.util.{Success, Try}

object ThreadPool extends ExecutionContextExecutor {
  import Util.checkError
  import LibUV.*
  import LibUVConstants.*

  val loop: Loop = uv_default_loop()
  val runnableMap = new mutable.HashMap[WorkReq, Runnable]

  val workCB: WorkCB = (req: WorkReq) => runnableMap(req).run()

  val afterWorkCB: AfterWorkCB =
    (req: WorkReq, status: Int) =>
      runnableMap -= req
      free(req.asInstanceOf[Ptr[Byte]])
      ()

  def execute(runnable: Runnable): Unit = {
    val req = malloc(uv_req_size(UV_WORK_REQ_T)).asInstanceOf[WorkReq]

    runnableMap(req) = runnable
    checkError(uv_queue_work(loop, req, workCB, afterWorkCB), "uv_queue_work")
  }

  def reportFailure(t: Throwable): Unit = {
//    println(s"Future failed with Throwable $t:")
    t.printStackTrace()
  }

  def schedule[R](process: => R): Future[R] = Future(process)(this)
}

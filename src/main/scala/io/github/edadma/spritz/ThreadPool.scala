package io.github.edadma.spritz

import scala.Option
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future, Promise}
import scala.scalanative.libc.stdlib._
import scala.scalanative.unsafe.*
import scala.util.{Success, Try}

object ThreadPool extends ExecutionContextExecutor:
  import Util.checkError
  import LibUV.*
  import LibUVConstants.*

  lazy val loop: Loop = uv_default_loop()
  lazy val lock: RWLock = malloc(RWLockSize)
  lazy val printlock: RWLock = malloc(RWLockSize)

  checkError(uv_rwlock_init(lock), "uv_rwlock_init")
  checkError(uv_rwlock_init(printlock), "uv_rwlock_init")

  val runnableMap = new mutable.HashMap[WorkReq, Runnable]

  def prt(a: Any): Unit =
    uv_rwlock_wrlock(printlock)
    println(a)
    uv_rwlock_wrunlock(printlock)

  def add(req: WorkReq, runnable: Runnable): Unit =
    uv_rwlock_wrlock(lock)
    runnableMap(req) = runnable
    uv_rwlock_wrunlock(lock)

  def get(req: WorkReq): Runnable =
    uv_rwlock_wrlock(lock)

    val runnable = runnableMap(req)

    uv_rwlock_wrunlock(lock)
    runnable

  def remove(req: WorkReq): Unit =
    uv_rwlock_wrlock(lock)
    runnableMap -= req
    uv_rwlock_wrunlock(lock)

  def scheduled: Boolean =
    uv_rwlock_wrlock(lock)
    prt("scheduled")

    val res = runnableMap.nonEmpty

    uv_rwlock_wrunlock(lock)
    res

  val workCB: WorkCB = (req: WorkReq) =>
    get(req).run()
    prt("workCB")

  val afterWorkCB: AfterWorkCB =
    (req: WorkReq, status: Int) =>
      prt("afterWorkCB")
      remove(req)
      free(req.asInstanceOf[Ptr[Byte]])
      ()

  def execute(runnable: Runnable): Unit = {
    val req = malloc(uv_req_size(UV_WORK_REQ_T)).asInstanceOf[WorkReq]

    prt("execute")
    add(req, runnable)
    checkError(uv_queue_work(loop, req, workCB, afterWorkCB), "uv_queue_work")
  }

  def reportFailure(t: Throwable): Unit = {
//    prt(s"Future failed with Throwable $t:")
    t.printStackTrace()
  }

  def apply[R](process: => R): Future[R] = Future(process)(this)

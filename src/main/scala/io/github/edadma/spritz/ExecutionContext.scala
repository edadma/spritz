package io.github.edadma.spritz

import scala.collection.mutable
import scala.collection.mutable.Queue
import scala.concurrent.ExecutionContextExecutor

trait ExecutionContext {

  /** Runs a block of code on this execution context. */
  def execute(runnable: Runnable): Unit

  /** Reports that an asynchronous computation failed. */
  def reportFailure(t: Throwable): Unit

}

object ExecutionContext {
  def global: ExecutionContextExecutor = QueueExecutionContext

  private object QueueExecutionContext extends ExecutionContextExecutor {
    def execute(runnable: Runnable): Unit = queue enqueue runnable
    def reportFailure(t: Throwable): Unit = t.printStackTrace()
  }

  private val queue: mutable.Queue[Runnable] = new mutable.Queue

  private def loop(): Unit = {
    while (queue.nonEmpty) {
      val runnable = queue.dequeue
      try {
        runnable.run()
      } catch {
        case t: Throwable =>
          QueueExecutionContext.reportFailure(t)
      }
    }
  }
}

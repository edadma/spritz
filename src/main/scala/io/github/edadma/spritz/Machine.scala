package io.github.edadma.spritz

abstract class Machine:
  val start: State

  case object NOT_STARTED extends State { def on = { case _ => } }
  case object DONE extends State { def on = { case _ => } }

  var received: Int = 0
  var started = false
  var state: State = NOT_STARTED
  var idx: Int = 0
  var trace = false

  def isDone: Boolean = state == DONE

  def pushback(): Unit = idx -= 1

  protected def goto(next: State): Unit =
    next.enter()
    state = next

  def selfTransition(): Unit = transition(state)

  def transition(next: State): Unit =
    if trace then println(s"$state => $next")
    if state != null then state.exit()
    goto(next)

  def directTransition(next: State): Unit =
    if trace then println(s"$state =direct> $next")
    goto(next)

  def run(input: Array[Byte]): Unit =
    if !started then
      started = true
      transition(start)

    while idx < input.length && state != DONE do
      val b = input(idx)

      if trace then
        println(s"$state <- $b (${if b == '\r' then "\\r" else if b == '\n' then "\\n" else b.toChar.toString})")

      idx += 1
      received += 1
      state on b
  end run

  abstract class State:
    def on: PartialFunction[Int, Unit]

    def enter(): Unit = {}
    def exit(): Unit = {}
  end State

  override def toString: String = s"machine state: $state, received: $received"
end Machine

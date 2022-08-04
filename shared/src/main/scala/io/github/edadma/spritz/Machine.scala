package io.github.edadma.spritz

abstract class Machine:
  val start: State

  val EOI: Int = -1
  var state: State = _
  var idx: Int = 0
  var trace = false

  def pushback(): Unit = idx -= 1

  case object DONE extends State { def on = { case _ => } }

  protected def goto(next: State): Unit =
    next.enter()
    state = next

  def transition(next: State): Unit =
    if trace then println(s"$state => $next")
    if state != null then state.exit()
    goto(next)

  def directTransition(next: State): Unit =
    if trace then println(s"$state =direct> $next")
    goto(next)

  def run(input: Array[Byte]): Unit =
    transition(start)

    while idx < input.length && state != DONE do
      val b = input(idx)

      if trace then
        println(s"$state <- $b (${if b == '\r' then "\\r" else if b == '\n' then "\\n" else b.toChar.toString})")

      idx += 1
      state on b

    if idx >= input.length then
      if trace then println(s"$state <- EOI")

      state on EOI

    transition(DONE)
end Machine

abstract class State:
  def on: PartialFunction[Int, Unit]

  def enter(): Unit = {}
  def exit(): Unit = {}
end State

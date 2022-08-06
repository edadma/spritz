package io.github.edadma.spritz

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class RequestParser extends Machine:
  val start: State = methodState

  val elems = new ListBuffer[String]
  val buf = new StringBuilder
  val body = new ArrayBuffer[Byte]

  def acc(b: Int): Unit = buf += b.toByte.toChar

  def br: Nothing = sys.error("bad request")

  abstract class AccState extends State:
    override def enter(): Unit = buf.clear()

    override def exit(): Unit =
      if buf.isEmpty then br
      elems += buf.toString

  class RequestLineState(next: State) extends AccState:
    def on = {
      case ' '               => transition(next)
      case EOI | '\r' | '\n' => br
      case b                 => acc(b)
    }

  case object valueState extends AccState:
    def on = {
      case '\r'       => transition(value2keyState)
      case EOI | '\n' => br
      case b          => acc(b)
    }

  case object value2keyState extends State:
    def on = {
      case '\n' => transition(keyState)
      case _    => br
    }

  case object keyState extends AccState:
    def on = {
      case '\r' if buf.nonEmpty => br
      case '\r'                 => directTransition(blankState)
      case ':'                  => transition(key2valueState)
      case EOI | '\n'           => br
      case b                    => acc(b)
    }

  case object blankState extends State:
    def on = {
      case '\n' => transition(bodyState)
      case _    => br
    }

  case object bodyState extends State:
    def on = {
      case EOI =>
      case b   => body += b.toByte
    }

  case object key2valueState extends State:
    def on = {
      case ' '               =>
      case EOI | '\r' | '\n' => br
      case _ =>
        pushback()
        transition(valueState)
    }

  case object methodState extends RequestLineState(pathState):
    override def enter(): Unit =
      elems.clear()
      body.clear()
      super.enter()

  case object pathState extends RequestLineState(valueState)
end RequestParser

package io.github.edadma.spritz

import LibUV.{uv_err_name, uv_strerror}

import java.nio.ByteBuffer
import scala.collection.mutable.ArrayBuffer
import scala.io.Codec
import scala.scalanative.unsafe.fromCString

object Util:

  def checkError(v: Int, label: String): Unit =
    if v != 0 then
      val error = fromCString(uv_err_name(v))
      val message = fromCString(uv_strerror(v))

      sys.error(s"$label error: $error: $message")

  def urlDecode(s: String, codec: Codec = Codec.UTF8): String =
    if s.indexOf('%') == -1 then s
    else
      val bytes = new ArrayBuffer[Byte]
      var idx = 0

      def hex(d: Char): Int =
        if '0' <= d && d <= '9' then d - '0'
        else if 'A' <= d && d <= 'F' then d - 'A' + 10
        else if 'a' <= d && d <= 'f' then d - 'a' + 10
        else sys.error(s"invalid hex digit")

      while idx < s.length do
        s(idx) match
          case '%' =>
            bytes += ((hex(s(idx + 1)) << 4) + hex(s(idx + 2))).toByte
            idx += 2
          case c => bytes += c.toByte

        idx += 1

      codec.charSet.decode(ByteBuffer.wrap(bytes.toArray)).toString
  end urlDecode

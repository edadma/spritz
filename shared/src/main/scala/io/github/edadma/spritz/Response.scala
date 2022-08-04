package io.github.edadma.spritz

import java.time.{ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter
import scala.collection.immutable
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Codec

class Response(serverName: String, zoneId: ZoneId = ZoneId.of("GMT")):
  var statusCode: Int = 500
  var statusMessage: String = HTTP.statusMessage(statusCode)
  val headers = new mutable.LinkedHashMap[String, String]
  var body: Array[Byte] = Array()

  def status(code: Int): Response =
    statusCode = code
    this

  def sendStatus(code: Int): Response =
    status(code).send(s"<h1>${HTTP.statusMessageString(code)}</h1>")
    this

  def send(text: String): Response =
    setIfNot("Content-Type") {
      if body startsWith "<" then "text/html; charset=UTF-8" else "text/text; charset=UTF-8"
    }

    body = Codec.toUTF8(text)
    headers("Content-Length") = body.length.toString
    this

  def setIfNot(key: String)(value: => String): Response =
    if !(headers contains key) then headers(key) = value
    this

  def response: Array[Byte] =
    setIfNot("Server") { serverName }
    setIfNot("Date") { DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(zoneId)) }

    val buf = new ArrayBuffer[Byte]

    def eol = buf ++= "\r\n".getBytes

    buf ++= s"HTTP/1.0 $statusCode $statusMessage".getBytes
    eol

    for (k, v) <- headers do
      buf ++= s"$k: $v".getBytes
      eol

    eol
    buf ++= body
    buf.toArray

  override def toString: String =
    s"--- HTTP Response Begin ---\n${Codec.fromUTF8(response).mkString}--- HTTP Response End ---"

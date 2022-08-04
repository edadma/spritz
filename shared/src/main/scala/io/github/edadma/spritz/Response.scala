package io.github.edadma.spritz

import scala.collection.mutable

class Response:
  var statusCode: Int = 0
  var statusMessage: String = ""
  val headers = new mutable.LinkedHashMap[String, String]
  var body: Array[Byte] = Array()

  def status(code: Int): Response =
    statusCode = code
    this

  def sendStatus(code: Int): Response =
    status(code).send(s"<h1>${HTTP.statusMessageString(code)}</h1>")
    this

  def send(s: String): Response =
    if !(headers contains "Content-Type") then
      headers("Content-Type") = if body startsWith "<" then "text/html; charset=UTF-8" else "text/text; charset=UTF-8"

    body = s
    this

  def response: String =
    s"""HTTP/1.0 $statusCode $statusMessage\r
       |
       |""".stripMargin

  override def toString: String = s"--- HTTP Response Begin ---\n$response--- HTTP Response End ---"

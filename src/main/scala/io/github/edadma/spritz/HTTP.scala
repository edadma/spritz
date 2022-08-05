package io.github.edadma.spritz

object HTTP:
  val statusMessage: Map[Int, String] =
    Map(
      100 -> "Continue",
      101 -> "Switching Protocols",
      102 -> "Processing",
      103 -> "Early Hints",
      200 -> "OK",
      201 -> "Created",
      202 -> "Accepted",
      203 -> "Non-Authoritative Information",
      204 -> "No Content",
      301 -> "Moved Permanently",
      400 -> "Bad Request",
      401 -> "Unauthorized",
      403 -> "Forbidden",
      404 -> "Not Found",
      418 -> "I'm a teapot",
      500 -> "Internal Server Error",
    )

  def statusMessageString(code: Int): String = statusMessage getOrElse (code, code.toString)

package io.github.edadma.spritz

import io.github.edadma.recognizer.CharRecognizer
import scala.collection.mutable.ListBuffer

class Router extends CharRecognizer[Char]:
  private val routes = new ListBuffer[Pattern]

//  def get(route: String): Router =

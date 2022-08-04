package io.github.edadma.spritz

import pprint.pprintln

@main def run(): Unit =
  pprintln(RouteParser("/asdf/:zxcv-:qwer"))

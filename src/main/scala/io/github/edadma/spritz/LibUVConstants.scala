package io.github.edadma.spritz

object LibUVConstants:
  // uv_run_mode
  val UV_RUN_DEFAULT = 0
  val UV_RUN_ONCE = 1
  val UV_RUN_NOWAIT = 2

  // UV_HANDLE_T
  val UV_PIPE_T = 7 // Pipes
  val UV_POLL_T = 8 // Polling external sockets
  val UV_PREPARE_T = 9 // Runs every loop iteration
  val UV_PROCESS_T = 10 // Subprocess
  val UV_TCP_T = 12 // TCP sockets
  val UV_TIMER_T = 13 // Timer
  val UV_TTY_T = 14 // Terminal emulator
  val UV_UDP_T = 15 // UDP sockets

  // UV_REQ_T
  val UV_WRITE_REQ_T = 3
  val UV_SHUTDOWN_REQ_T = 4
  val UV_FS_REQ_T = 6
  val UV_WORK_REQ_T = 7

  val UV_READABLE = 1
  val UV_WRITABLE = 2
  val UV_DISCONNECT = 4
  val UV_PRIORITIZED = 8

  val O_RDWR = 2
  val O_CREAT = sys.props("os.name") match {
    case "Mac OS X" => 512
    case _          => 64
  }
  val default_permissions = 420 // octal 0644

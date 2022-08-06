package io.github.edadma.spritz

import scala.scalanative.unsafe.{CFuncPtr1, CFuncPtr2, CFuncPtr3, CSSize, CSize, CString, CStruct2, Ptr, extern, link}

@link("uv")
@extern
object libuv {
  type TimerHandle = Ptr[Byte]
  type PipeHandle = Ptr[Ptr[Byte]]

  type Loop = Ptr[Byte]
  type TimerCB = CFuncPtr1[TimerHandle, Unit]

  def uv_default_loop(): Loop = extern
  def uv_loop_size(): CSize = extern
  def uv_is_active(handle: Ptr[Byte]): Int = extern
  def uv_handle_size(h_type: Int): CSize = extern
  def uv_req_size(r_type: Int): CSize = extern

  def uv_timer_init(loop: Loop, handle: TimerHandle): Int = extern
  def uv_timer_start(handle: TimerHandle, cb: TimerCB, timeout: Long, repeat: Long): Int = extern
  def uv_timer_stop(handle: TimerHandle): Int = extern

  def uv_run(loop: Loop, runMode: Int): Int = extern

  def uv_strerror(err: Int): CString = extern
  def uv_err_name(err: Int): CString = extern

  type Buffer = CStruct2[Ptr[Byte], CSize]
  type TCPHandle = Ptr[Ptr[Byte]]
  type uv_connection_cb = CFuncPtr2[TCPHandle, Int, Unit]
  type WriteReq = Ptr[Ptr[Byte]]
  type ShutdownReq = Ptr[Ptr[Byte]]

  def uv_tcp_init(loop: Loop, tcp_handle: TCPHandle): Int = extern

  def uv_tcp_bind(tcp_handle: TCPHandle, address: Ptr[Byte], flags: Int): Int = extern
  def uv_listen(stream_handle: TCPHandle, backlog: Int, uv_connection_cb: uv_connection_cb): Int = extern
  def uv_accept(server: TCPHandle, client: TCPHandle): Int = extern

  def uv_read_start(stream: TCPHandle, uv_alloc_cb: uv_alloc_cb, uv_read_cb: uv_read_cb): Int = extern
  def uv_write(writeReq: WriteReq, client: TCPHandle, bufs: Ptr[Buffer], numBufs: Int, writeCB: WriteCB): Int = extern
  def uv_shutdown(shutdownReq: ShutdownReq, client: TCPHandle, shutdownCB: ShutdownCB): Int = extern
  def uv_close(handle: TCPHandle, closeCB: CloseCB): Int = extern

  def uv_ip4_addr(address: CString, port: Int, out_addr: Ptr[Byte]): Int = extern
  def uv_ip4_name(address: Ptr[Byte], s: CString, size: Int): Int = extern

  type uv_alloc_cb = CFuncPtr3[TCPHandle, CSize, Ptr[Buffer], Unit]
  type uv_read_cb = CFuncPtr3[TCPHandle, CSSize, Ptr[Buffer], Unit]
  type WriteCB = CFuncPtr2[WriteReq, Int, Unit]
  type ShutdownCB = CFuncPtr2[ShutdownReq, Int, Unit]
  type CloseCB = CFuncPtr1[TCPHandle, Unit]
}

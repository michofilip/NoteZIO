package zote.pages

import com.raquo.laminar.api.L.{*, given}
import sttp.client3.*
import sttp.client3.impl.zio.FetchZioBackend
import sttp.tapir.client.sttp.SttpClientInterpreter
import zio.*
import zote.dto.NoteHeader
import zote.endpoints.NoteEndpoints

object NoteListPage {
  def apply() = {

    lazy val backend = FetchZioBackend()
    lazy val interpreter = SttpClientInterpreter()
    lazy val uri = Some(uri"http://localhost:8080")
    lazy val endpoints = new NoteEndpoints {}

    val noteHeaders = Var[List[NoteHeader]](initial = List.empty)

    def getAllNoteHeadersRequest =
      interpreter.toRequestThrowDecodeFailures(endpoints.getAllEndpoint, uri)

    def getAllNoteHeadersRequestZIO() = {
      backend
        .send(getAllNoteHeadersRequest(()))
        .map(_.body)
        .absolve
    }

    def performRequest() = {
      Unsafe.unsafe { implicit unsafe =>
        Runtime.default.unsafe.fork(
          getAllNoteHeadersRequestZIO().tap(value =>
            ZIO.attempt(noteHeaders.set(value))
          )
        )
      }
    }

    div(
      onMountCallback(_ => performRequest()),
      div("Note list"),
      div(child.text <-- noteHeaders.signal.map(_.toString()))
    )
  }
}

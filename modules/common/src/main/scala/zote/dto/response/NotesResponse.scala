package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Message, NoteHeader}
import zote.enums.ResponseStatus

case class NotesResponse(
    override val status: ResponseStatus,
    override val data: Option[List[NoteHeader]] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[List[NoteHeader]] derives JsonCodec

object NotesResponse {
  given ResponseInitializer[List[NoteHeader], NotesResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[List[NoteHeader]],
        messages: Option[Set[Message]],
    ): NotesResponse = {
      NotesResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

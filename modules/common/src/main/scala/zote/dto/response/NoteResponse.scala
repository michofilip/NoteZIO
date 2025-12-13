package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Message, Note}
import zote.enums.ResponseStatus

case class NoteResponse(
    override val status: ResponseStatus,
    override val data: Option[Note] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[Note] derives JsonCodec

object NoteResponse {
  given ResponseInitializer[Note, NoteResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[Note],
        messages: Option[Set[Message]],
    ): NoteResponse = {
      NoteResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

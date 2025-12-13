package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Label, Message}
import zote.enums.ResponseStatus

case class LabelResponse(
    override val status: ResponseStatus,
    override val data: Option[Label] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[Label] derives JsonCodec

object LabelResponse {
  given ResponseInitializer[Label, LabelResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[Label],
        messages: Option[Set[Message]],
    ): LabelResponse = {
      LabelResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

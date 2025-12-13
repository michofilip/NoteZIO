package zote.dto.response

import zio.json.JsonCodec
import zote.dto.Message
import zote.dto.response.Response.ResponseInitializer
import zote.enums.ResponseStatus

case class EmptyResponse(
    override val status: ResponseStatus,
    override val messages: Option[Set[Message]] = None,
) extends Response[Nothing] derives JsonCodec {
  override def data: Option[Nothing] = None
}

object EmptyResponse {
  given ResponseInitializer[Nothing, EmptyResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[Nothing],
        messages: Option[Set[Message]],
    ): EmptyResponse = {
      EmptyResponse(
        status = status,
        messages = messages,
      )
    }
  }
}

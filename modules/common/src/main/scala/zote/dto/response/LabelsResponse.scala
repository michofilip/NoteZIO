package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Label, Message}
import zote.enums.ResponseStatus

case class LabelsResponse(
    override val status: ResponseStatus,
    override val data: Option[List[Label]] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[List[Label]] derives JsonCodec

object LabelsResponse {
  given ResponseInitializer[List[Label], LabelsResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[List[Label]],
        messages: Option[Set[Message]],
    ): LabelsResponse = {
      LabelsResponse(
        status = ResponseStatus.Success,
        data = data,
        messages = messages,
      )
    }
  }
}

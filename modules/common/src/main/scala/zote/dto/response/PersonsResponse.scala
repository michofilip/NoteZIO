package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Message, Person}
import zote.enums.ResponseStatus

case class PersonsResponse(
    override val status: ResponseStatus,
    override val data: Option[List[Person]] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[List[Person]] derives JsonCodec

object PersonsResponse {
  given ResponseInitializer[List[Person], PersonsResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[List[Person]],
        messages: Option[Set[Message]],
    ): PersonsResponse = {
      PersonsResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

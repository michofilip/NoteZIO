package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Message, Person}
import zote.enums.ResponseStatus

case class PersonResponse(
    override val status: ResponseStatus,
    override val data: Option[Person] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[Person] derives JsonCodec

object PersonResponse {
  given ResponseInitializer[Person, PersonResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[Person],
        messages: Option[Set[Message]],
    ): PersonResponse = {
      PersonResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Message, User}
import zote.enums.ResponseStatus

case class UsersResponse(
    override val status: ResponseStatus,
    override val data: Option[List[User]] = None,
    override val messages: Option[Set[Message]] = None,
) extends Response[List[User]] derives JsonCodec

object UsersResponse {
  given ResponseInitializer[List[User], UsersResponse] {
    override def init(
        status: ResponseStatus,
        data: Option[List[User]],
        messages: Option[Set[Message]],
    ): UsersResponse = {
      UsersResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

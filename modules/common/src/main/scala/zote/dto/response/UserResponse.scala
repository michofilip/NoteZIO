package zote.dto.response

import zio.json.JsonCodec
import zote.dto.response.Response.ResponseInitializer
import zote.dto.{Message, User}
import zote.enums.ResponseStatus

case class UserResponse(
                           override val status: ResponseStatus,
                           override val data: Option[User] = None,
                           override val messages: Option[Set[Message]] = None,
) extends Response[User] derives JsonCodec

object UserResponse {
  given ResponseInitializer[User, UserResponse] {
    override def init(
                       status: ResponseStatus,
                       data: Option[User],
                       messages: Option[Set[Message]],
    ): UserResponse = {
      UserResponse(
        status = status,
        data = data,
        messages = messages,
      )
    }
  }
}

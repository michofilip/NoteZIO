package zote.dto.response

import sttp.model.StatusCode
import zote.dto.Message
import zote.dto.response.Response.ResponseInitializer
import zote.enums.ResponseStatus
import zote.exceptions.{NotFoundException, ValidationException}

trait Response[T] {
  def status: ResponseStatus
  def data: Option[T]
  def messages: Option[Set[Message]]

  final def withMessages[Res <: Response[T]](messages: Set[Message])(using ResponseInitializer[T, Res]): Res = {
    val maybeMessages = Option.when(messages.nonEmpty)(messages)
    status match {
      case ResponseStatus.Success =>
        summon[ResponseInitializer[T, Res]].init(ResponseStatus.Success, data, maybeMessages)
      case ResponseStatus.Failure =>
        summon[ResponseInitializer[T, Res]].init(ResponseStatus.Failure, None, maybeMessages)
    }
  }

  final def withMessage[Res <: Response[T]](message: Message)(using ResponseInitializer[T, Res]): Res = {
    withMessages(Set(message))
  }
}

object Response {
  trait ResponseInitializer[T, Res <: Response[T]] {
    def init(status: ResponseStatus, data: Option[T], messages: Option[Set[Message]]): Res
  }

  def success[T, Res <: Response[T]](using ResponseInitializer[T, Res]): Res = {
    summon[ResponseInitializer[T, Res]].init(ResponseStatus.Success, None, None)
  }

  def success[T, Res <: Response[T]](data: T)(using ResponseInitializer[T, Res]): Res = {
    summon[ResponseInitializer[T, Res]].init(ResponseStatus.Success, Some(data), None)
  }

  def failure[T, Res <: Response[T]](using ResponseInitializer[T, Res]): Res = {
    summon[ResponseInitializer[T, Res]].init(ResponseStatus.Failure, None, None)
  }

  def decode[R <: Response[?]](tuple: (StatusCode, R)): Throwable = tuple match {
    case (_, response) => new RuntimeException(response.messages.mkString(", "))
  }

  def encode[T, Res <: Response[T]](error: Throwable)(using ResponseInitializer[T, Res]): (StatusCode, Res) = {
    error match {
      case e: NotFoundException   => (StatusCode.NotFound, Response.failure.withMessage(Message.error(e.message)))
      case e: ValidationException =>
        (StatusCode.UnprocessableEntity, Response.failure.withMessages(e.messages.map(Message.error)))
      case e => (StatusCode.InternalServerError, Response.failure.withMessage(Message.error(e.getMessage)))
    }
  }
}

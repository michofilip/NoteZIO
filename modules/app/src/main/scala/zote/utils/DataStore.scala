package zote.utils

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Message
import zote.dto.response.Response
import zote.enums.ResponseStatus

trait DataStore[T] {
  protected val data: Var[Option[T]] = Var(None)

  def set(value: T): Unit         = data.set(Some(value))
  def get: Signal[Option[T]]      = data.signal
  def clear(): Unit               = data.set(None)
  def isEmpty: Signal[Boolean]    = get.map(_.isEmpty)
  def isNotEmpty: Signal[Boolean] = get.map(_.nonEmpty)
}

trait Fetch { this: DataStore[?] =>
  def fetch(): Unit
}

trait FetchById[Id] { this: DataStore[?] =>
  def fetch(id: Id): Unit
}

trait ResponseService[T <: Response[?]] extends DataStore[T] {
  def status: Signal[Option[ResponseStatus]] = get.map(_.map(_.status))
  def messages: Signal[Option[Set[Message]]] = get.map(_.flatMap(_.messages))
}

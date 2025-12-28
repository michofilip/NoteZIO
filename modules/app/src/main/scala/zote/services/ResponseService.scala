package zote.services

import com.raquo.laminar.api.L.{*, given}

trait ResponseService[T] {
  protected val data: Var[Option[T]] = Var(None)

  def set(value: T): Unit = data.set(Some(value))

  def get: Signal[Option[T]] = data.signal

  def clear(): Unit = data.set(None)
}

trait Fetch {
  def fetch(): Unit
}

trait FetchById[Id] {
  def fetch(id: Id): Unit
}

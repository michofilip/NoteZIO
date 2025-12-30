package zote.utils

import com.raquo.laminar.api.L.{*, given}

trait DataStore[T] {
  protected val data: Var[Option[T]] = Var(None)

  def set(value: T): Unit = data.set(Some(value))

  def get: Signal[Option[T]] = data.signal

  def clear(): Unit = data.set(None)
}

trait Fetch { this: DataStore[?] =>
  def fetch(): Unit
}

trait FetchById[Id] { this: DataStore[?] =>
  def fetch(id: Id): Unit
}

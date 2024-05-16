package zote.db

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

case class QuillContext(postgres: Quill.Postgres[SnakeCase])

object QuillContext {
  lazy val layer = Quill.Postgres.fromNamingStrategy(SnakeCase) >>> ZLayer.derive[QuillContext]
}

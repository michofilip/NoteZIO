package zote.db

import io.getquill.jdbczio.Quill
import io.getquill.{NamingStrategy, SnakeCase}
import zio.ZLayer

import javax.sql.DataSource

case class QuillContext(postgres: Quill.Postgres[SnakeCase])

object QuillContext {
    lazy val layer = Quill.Postgres.fromNamingStrategy(SnakeCase) >>> ZLayer.derive[QuillContext]
}

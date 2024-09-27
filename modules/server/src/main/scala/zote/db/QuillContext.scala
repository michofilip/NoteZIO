package zote.db

import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

case class QuillContext(override val ds: DataSource)
    extends Quill.H2(SnakeCase, ds)

object QuillContext {
  lazy val layer = ZLayer.derive[QuillContext]
}

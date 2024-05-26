package zote.config

import io.getquill.jdbczio.Quill

object DataSourceConfig {
  lazy val layer = Quill.DataSource.fromPrefix("db")
}

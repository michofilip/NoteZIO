package zote

import zio.{ZIO, ZIOAppDefault, ZLayer}
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.services.FlywayService

object Flyway extends ZIOAppDefault {
  def run = FlywayService.run
    .provide(
      FlywayService.layer,
      FlywayConfig.layer,
      DataSourceConfig.layer
    )
    .exitCode

}

package zote

import zio.{ZIO, ZIOAppDefault, ZLayer}
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.services.{FlywayService, FlywayServiceImpl}

object Flyway extends ZIOAppDefault {
  def run = FlywayService.run
    .provide(
      FlywayServiceImpl.layer,
      FlywayConfig.layer,
      DataSourceConfig.layer
    )
    .exitCode

}

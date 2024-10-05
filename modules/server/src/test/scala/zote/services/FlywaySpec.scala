package zote.services

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}

object FlywaySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("FlywaySpec")(
      test("Flyway") {
        for {
          _ <- FlywayService.run
        } yield assertTrue(true)
      }
    )
  }.provide(
    FlywayServiceImpl.layer,
    FlywayConfig.layer,
    DataSourceConfig.layer
//    ZPostgreSQLContainer.Settings.default,
//    ZPostgreSQLContainer.live
  )
}

package zote

import zio.*
import zote.config.{DbConfig, FlywayConfig}
import zote.services.FlywayService

object FlywayApp extends ZIOAppDefault {

    private val app = for {
        _ <- FlywayService.run
    } yield ()

    def run = app.provide(
        FlywayService.layer,
        FlywayConfig.layer,
        DbConfig.layer,
    ).exitCode
}

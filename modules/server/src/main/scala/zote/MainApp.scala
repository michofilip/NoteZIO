package zote

import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.Server
import zote.config.*
import zote.controllers.*
import zote.db.QuillContext
import zote.db.repositories.*
import zote.services.*

object MainApp extends ZIOAppDefault {

  private val app = for {
    _ <- FlywayService.run

    _ <- ZIO.logInfo("Welcome to Zote")

    routes <- HttpApi.routesZIO
    swaggerRoutes <- SwaggerApi.routesZIO
    port <- Server.install(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default
      ).toHttp(routes ++ swaggerRoutes)
    )
    _ <- ZIO.logInfo(s"Server started at port: $port")

    _ <- ZIO.never
  } yield ()

  def run = app
    .provide(
      FlywayServiceImpl.layer,
      FlywayConfig.layer,
      HealthController.layer,
      NoteController.layer,
      PersonController.layer,
      LabelController.layer,
      NoteServiceImpl.layer,
      LabelServiceImpl.layer,
      PersonServiceImpl.layer,
      NoteRepositoryImpl.layer,
      LabelRepositoryImpl.layer,
      PersonRepositoryImpl.layer,
      NotePersonRepositoryImpl.layer,
      NoteLabelRepositoryImpl.layer,
      QuillContext.layer,
      ServerConfig.layer,
      SLF4JConfig.layer,
      DataSourceConfig.layer,
      ZLayer.Debug.mermaid
    )
    .exitCode
}

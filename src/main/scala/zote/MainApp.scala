package zote

import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.*
import zio.http.Server
import zote.config.*
import zote.controllers.{HealthController, HttpApi, NoteController, UserController}
import zote.db.QuillContext
import zote.db.repositories.{NoteRepository, UserRepository}
import zote.services.{FlywayService, FlywayServiceImpl, NoteService, UserService}

object MainApp extends ZIOAppDefault {

    private val app = for {
        _ <- FlywayService.run
        _ <- ZIO.logInfo("Welcome to Zote")

        routes <- HttpApi.routesZIO
        endpoints <- HttpApi.endpointsZIO
        port <- Server.install(
            ZioHttpInterpreter(
                ZioHttpServerOptions.default
            ).toHttp(routes ++ SwaggerInterpreter().fromEndpoints[Task](endpoints, "Zote", "0.1.0-SNAPSHOT"))
        )
        _ <- ZIO.logInfo(s"Server started at port: $port")

        _ <- ZIO.never
    } yield ()

    def run = app.provide(
        HealthController.layer,
        NoteController.layer,
        UserController.layer,

        NoteService.layer,
        UserService.layer,

        NoteRepository.layer,
        UserRepository.layer,

        QuillContext.layer,

        FlywayService.layer,
        ServerConfig.layer,
        SLF4JConfig.layer,
        DbConfig.layer,
        FlywayConfig.layer,

        ZLayer.Debug.mermaid
    ).exitCode
}

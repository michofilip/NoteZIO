package zote

import zio.*
import zote.config.*
import zote.controllers.*
import zote.db.QuillContext
import zote.db.repositories.*
import zote.server.AppServer
import zote.services.*
import zote.services.validation.{LabelValidationServiceImpl, NoteValidationServiceImpl, UserValidationServiceImpl}

object Main extends ZIOAppDefault {

  private val app = for {
    _    <- FlywayService.run
    _    <- InitHelper.initDb()
    port <- ZIO.serviceWithZIO[AppServer](_.install)
    _    <- ZIO.logInfo("Welcome to Zote")
    _    <- ZIO.logInfo(s"Server started at port: $port")
    _    <- ZIO.never
  } yield ()

  def run = app
    .provide(
      AppServer.layer,
      FlywayServiceImpl.layer,
      FlywayConfig.layer,
      NoteController.layer,
      UserController.layer,
      LabelController.layer,
      NoteServiceImpl.layer,
      LabelServiceImpl.layer,
      UserServiceImpl.layer,
      NoteValidationServiceImpl.layer,
      UserValidationServiceImpl.layer,
      LabelValidationServiceImpl.layer,
      NoteRepositoryImpl.layer,
      LabelRepositoryImpl.layer,
      UserRepositoryImpl.layer,
      NoteUserRepositoryImpl.layer,
      NoteLabelRepositoryImpl.layer,
      QuillContext.layer,
      ServerConfig.layer,
      SLF4JConfig.layer,
      DataSourceConfig.layer,
      InitHelper.layer,
//      ZLayer.Debug.mermaid,
    )
}

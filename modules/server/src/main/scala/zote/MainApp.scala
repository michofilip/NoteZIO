package zote

import zio.*
import zote.config.*
import zote.controllers.*
import zote.db.QuillContext
import zote.db.repositories.*
import zote.server.AppServer
import zote.services.*
import zote.services.validation.{LabelValidationServiceImpl, NoteValidationServiceImpl, PersonValidationServiceImpl}

object MainApp extends ZIOAppDefault {

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
      PersonController.layer,
      LabelController.layer,
      NoteServiceImpl.layer,
      LabelServiceImpl.layer,
      PersonServiceImpl.layer,
      NoteValidationServiceImpl.layer,
      PersonValidationServiceImpl.layer,
      LabelValidationServiceImpl.layer,
      NoteRepositoryImpl.layer,
      LabelRepositoryImpl.layer,
      PersonRepositoryImpl.layer,
      NotePersonRepositoryImpl.layer,
      NoteLabelRepositoryImpl.layer,
      QuillContext.layer,
      ServerConfig.layer,
      SLF4JConfig.layer,
      DataSourceConfig.layer,
      InitHelper.layer,
//      ZLayer.Debug.mermaid,
    )
}

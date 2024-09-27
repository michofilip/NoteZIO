package zote.db.repositories

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.services.FlywayService

object PersonRepositorySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("PersonRepository")(
      suite("provides function 'findById' that")(
        test("returns option with personEntity if exists") {
          for {
            _ <- FlywayService.run
            personRepository <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(1)
          } yield assertTrue(maybePersonEntity.isDefined)
        },
        test("returns empty option if not exists") {
          for {
            _ <- FlywayService.run
            personRepository <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(-1)
          } yield assertTrue(maybePersonEntity.isEmpty)
        }
      )
    )
  }.provide(
    FlywayService.layer,
    FlywayConfig.layer,
    PersonRepository.layer,
    QuillContext.layer,
    DataSourceConfig.layer
  )
}

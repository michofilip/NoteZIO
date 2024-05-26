package zote.db.repositories

import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import zio.*
import zio.test.*
import zio.test.TestAspect.sequential
import zote.db.QuillContext

object PersonRepositorySpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("PersonRepository")(
      suite("provides function 'findById' that")(
        test("returns option with personEntity if exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(1)
          } yield assertTrue(maybePersonEntity.isDefined)
        },
        test("returns empty option if not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(-1)
          } yield assertTrue(maybePersonEntity.isEmpty)
        }
      )
    ) @@ DbMigrationAspect.migrate(
      """filesystem:src/main/resources/database/migrations"""
    )() @@ sequential
  }.provide(
    PersonRepository.layer,
    QuillContext.layer,
    ZPostgreSQLContainer.Settings.default,
    ZPostgreSQLContainer.live
  )
}

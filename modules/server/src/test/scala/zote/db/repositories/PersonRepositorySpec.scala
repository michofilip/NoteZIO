package zote.db.repositories

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object PersonRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("PersonRepository")(
      suite("provides function 'findAll' that")(
        test("returns list of PersonEntities if some exist") {
          for {
            person1 <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            person2 <- DbHelper.insertPerson(PersonEntity(name = "Ela"))

            personRepository <- ZIO.service[PersonRepository]
            personEntities <- personRepository.findAll
          } yield assertTrue {
            personEntities.size == 2
            && personEntities.contains(person1)
            && personEntities.contains(person2)
          }
        },
        test("returns empty list if none exist") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            personEntities <- personRepository.findAll
          } yield assertTrue {
            personEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findById' that")(
        test("returns option with PersonEntity if exists") {
          for {
            person <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository
              .findById(person.id)
          } yield assertTrue {
            maybePersonEntity.contains(person)
          }
        },
        test("returns empty option if not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(-1)
          } yield assertTrue(maybePersonEntity.isEmpty)
        }
      ),
      suite("provides function 'getById' that")(
        test("returns PersonEntity if exists") {
          for {
            person <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository <- ZIO.service[PersonRepository]
            personEntity <- personRepository.getById(person.id)
          } yield assertTrue(personEntity == person)
        },
        test("returns NotFoundException if not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            result <- personRepository
              .getById(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Person id: -1 not found"
              case _ => false
          }
        }
      ),
      suite("provides function 'upsert' that")(
        test("inserts and returns PersonEntity if not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            personEntity <- personRepository.upsert(PersonEntity(name = "Hela"))
          } yield assertTrue(
            personEntity.id != 0 && personEntity.name == "Hela"
          )
        },
        test("updates and returns PersonEntity if exists") {
          for {
            person <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository <- ZIO.service[PersonRepository]
            personEntity <- personRepository.upsert(person.copy(name = "Hela"))
          } yield assertTrue(
            personEntity.id == person.id && personEntity.name == "Hela"
          )
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes PersonEntity if exists") {
          for {
            person <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository <- ZIO.service[PersonRepository]
            _ <- personRepository.delete(person.id)
            maybePersonEntity <- personRepository.findById(person.id)
          } yield assertTrue(maybePersonEntity.isEmpty)
        },
        test("does nothing if PersonEntity not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            result <- personRepository.delete(-1).fold(_ => false, _ => true)
          } yield assertTrue(result)
        }
      )
    )
      @@ TestAspectUtils.rollback
      @@ TestAspect.beforeAll(FlywayService.run)
      @@ TestAspect.sequential
  }.provide(
    FlywayServiceImpl.layer,
    FlywayConfig.layer,
    PersonRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

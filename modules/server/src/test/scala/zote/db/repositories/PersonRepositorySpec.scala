package zote.db.repositories

import zio.*
import zio.test.*
import zote.Ids.PersonId
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, TestAspectUtils}
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
            personEntities   <- personRepository.findAll
          } yield assertTrue {
            personEntities.size == 2 &&
            personEntities.contains(person1) &&
            personEntities.contains(person2)
          }
        },
        test("returns empty list if none exist") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            personEntities   <- personRepository.findAll
          } yield assertTrue {
            personEntities.isEmpty
          }
        },
      ),
      suite("provides function 'findById' that")(
        test("returns option with PersonEntity if exists") {
          for {
            person            <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository  <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(person.id)
          } yield assertTrue {
            maybePersonEntity.contains(person)
          }
        },
        test("returns empty option if not exists") {
          for {
            personRepository  <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findById(PersonId(-1))
          } yield assertTrue {
            maybePersonEntity.isEmpty
          }
        },
      ),
      suite("provides function 'getById' that")(
        test("returns PersonEntity if exists") {
          for {
            person           <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository <- ZIO.service[PersonRepository]
            personEntity     <- personRepository.getById(person.id)
          } yield assertTrue {
            personEntity == person
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            result           <- personRepository.getById(PersonId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Person id: -1 not found"))
          }
        },
      ),
      suite("provides function 'findByName' that")(
        test("returns option with PersonEntity if exists") {
          for {
            person            <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository  <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findByName(person.name)
          } yield assertTrue {
            maybePersonEntity.contains(person)
          }
        },
        test("returns empty option if not exists") {
          for {
            personRepository  <- ZIO.service[PersonRepository]
            maybePersonEntity <- personRepository.findByName("Ala")
          } yield assertTrue {
            maybePersonEntity.isEmpty
          }
        },
      ),
      suite("provides function 'upsert' that")(
        test("inserts and returns PersonEntity if not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            personEntity     <- personRepository.upsert(PersonEntity(name = "Ela"))
          } yield assertTrue {
            !personEntity.id.isZero &&
            personEntity.name == "Ela"
          }
        },
        test("updates and returns PersonEntity if exists") {
          for {
            person           <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository <- ZIO.service[PersonRepository]
            personEntity     <- personRepository.upsert(person.copy(name = "Ela"))
          } yield assertTrue {
            personEntity.id == person.id &&
            personEntity.name == "Ela"
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes PersonEntity if exists") {
          for {
            person                        <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personRepository              <- ZIO.service[PersonRepository]
            maybePersonEntityBeforeDelete <- personRepository.findById(person.id)
            _                             <- personRepository.delete(person.id)
            maybePersonEntityAfterDelete  <- personRepository.findById(person.id)
          } yield assertTrue {
            maybePersonEntityBeforeDelete.isDefined &&
            maybePersonEntityAfterDelete.isEmpty
          }
        },
        test("does nothing if PersonEntity not exists") {
          for {
            personRepository <- ZIO.service[PersonRepository]
            result           <- personRepository.delete(PersonId(-1)).exit
          } yield assertTrue {
            result.isSuccess
          }
        },
      ),
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
    DbHelper.layer,
  )
}

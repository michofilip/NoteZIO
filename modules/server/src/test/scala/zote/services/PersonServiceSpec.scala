package zote.services

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NotePersonEntity, PersonEntity}
import zote.db.repositories.{NotePersonRepositoryImpl, PersonRepositoryImpl}
import zote.dto.Person
import zote.dto.form.PersonForm
import zote.enums.{NotePersonRole, NoteStatus}
import zote.exceptions.{NotFoundException, ValidationException}
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}

object PersonServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("PersonService")(
      suite("provides function 'getAll' that")(
        test("returns list of Persons if some exist") {
          for {
            personEntity1 <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personEntity2 <- DbHelper.insertPerson(PersonEntity(name = "Ela"))

            personService <- ZIO.service[PersonService]
            persons <- personService.getAll
          } yield assertTrue {
            persons.size == 2
            && persons.contains(
              Person(id = personEntity1.id, name = personEntity1.name)
            )
            && persons.contains(
              Person(id = personEntity2.id, name = personEntity2.name)
            )
          }
        },
        test("returns empty list if none exist") {
          for {
            personService <- ZIO.service[PersonService]
            persons <- personService.getAll
          } yield assertTrue {
            persons.isEmpty
          }
        }
      ),
      suite("provides function 'getById' that")(
        test("returns Label if exists") {
          for {
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personService <- ZIO.service[PersonService]
            person <- personService.getById(personEntity.id)
          } yield assertTrue(person.name == personEntity.name)
        },
        test("returns NotFoundException if not exists") {
          for {
            personService <- ZIO.service[PersonService]
            result <- personService
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
      suite("provides function 'create' that")(
        test("creates and returns Person") {
          for {
            personService <- ZIO.service[PersonService]
            person <- personService.create(PersonForm(name = "Ala"))
          } yield assertTrue(person.name == "Ala")
        }
      ),
      suite("provides function 'update' that")(
        test("updates and returns Person") {
          for {
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personService <- ZIO.service[PersonService]
            person <- personService.update(
              personEntity.id,
              PersonForm(name = "Hela")
            )
          } yield assertTrue(person.name == "Hela")
        },
        test("returns NotFoundException if not exists") {
          for {
            personService <- ZIO.service[PersonService]
            result <- personService
              .update(-1, PersonForm(name = "Hela"))
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
      suite("provides function 'delete' that")(
        test("deletes Person") {
          for {
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            notePersonEntity <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = noteEntity.id,
                personId = personEntity.id,
                role = NotePersonRole.Owner
              )
            )

            personService <- ZIO.service[PersonService]
            _ <- personService.delete(personEntity.id)
            result <- personService
              .getById(personEntity.id)
              .fold(_ => true, _ => false)
          } yield assertTrue(result)
        },
        test("returns NotFoundException if not exists") {
          for {
            personService <- ZIO.service[PersonService]
            result <- personService
              .delete(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Person id: -1 not found"
              case _ => false
          }
        }
      )
    )
      @@ TestAspectUtils.rollback
      @@ TestAspect.beforeAll(FlywayService.run)
      @@ TestAspect.sequential
  }.provide(
    FlywayServiceImpl.layer,
    FlywayConfig.layer,
    PersonServiceImpl.layer,
    PersonRepositoryImpl.layer,
    NotePersonRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

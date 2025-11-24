package zote.services

import zio.*
import zio.test.*
import zote.Ids.PersonId
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NotePersonEntity, PersonEntity}
import zote.db.repositories.{NotePersonRepositoryImpl, PersonRepositoryImpl}
import zote.dto.Person
import zote.dto.form.PersonForm
import zote.enums.{NotePersonRole, NoteStatus}
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, TestAspectUtils}

object PersonServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("PersonService")(
      suite("provides function 'getAll' that")(
        test("returns list of Persons if some exist") {
          for {
            personEntity1 <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personEntity2 <- DbHelper.insertPerson(PersonEntity(name = "Ela"))

            personService <- ZIO.service[PersonService]
            persons       <- personService.getAll
          } yield assertTrue {
            persons.size == 2 &&
            persons.contains(Person(id = personEntity1.id, name = personEntity1.name)) &&
            persons.contains(Person(id = personEntity2.id, name = personEntity2.name))
          }
        },
        test("returns empty list if none exist") {
          for {
            personService <- ZIO.service[PersonService]
            persons       <- personService.getAll
          } yield assertTrue {
            persons.isEmpty
          }
        },
      ),
      suite("provides function 'getById' that")(
        test("returns Person if exists") {
          for {
            personEntity  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personService <- ZIO.service[PersonService]
            person        <- personService.getById(personEntity.id)
          } yield assertTrue {
            person.id == personEntity.id &&
            person.name == personEntity.name
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            personService <- ZIO.service[PersonService]
            result        <- personService.getById(PersonId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Person id: -1 not found"))
          }
        },
      ),
      suite("provides function 'create' that")(
        test("creates and returns Person") {
          for {
            personService <- ZIO.service[PersonService]
            person        <- personService.create(PersonForm(name = "Ala"))
          } yield assertTrue {
            !person.id.isZero &&
            person.name == "Ala"
          }
        },
      ),
      suite("provides function 'update' that")(
        test("updates and returns Person") {
          for {
            personEntity  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personService <- ZIO.service[PersonService]
            person <- personService.update(
              personEntity.id,
              PersonForm(name = "Ela"),
            )
          } yield assertTrue {
            person.id == personEntity.id &&
            person.name == "Ela"
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            personService <- ZIO.service[PersonService]
            result        <- personService.update(PersonId(-1), PersonForm(name = "Ela")).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Person id: -1 not found"))
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes Person") {
          for {
            personEntity <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = Some("Message 1"),
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            notePersonEntity <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = noteEntity.id,
                personId = personEntity.id,
                role = NotePersonRole.Owner,
              ),
            )

            personService      <- ZIO.service[PersonService]
            resultBeforeDelete <- personService.getById(personEntity.id).exit
            _                  <- personService.delete(personEntity.id)
            resultAfterDelete  <- personService.getById(personEntity.id).exit
          } yield assertTrue {
            resultBeforeDelete.isSuccess &&
            resultAfterDelete.isFailure
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            personService <- ZIO.service[PersonService]
            result        <- personService.delete(PersonId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Person id: -1 not found"))
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
    PersonServiceImpl.layer,
    PersonRepositoryImpl.layer,
    NotePersonRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

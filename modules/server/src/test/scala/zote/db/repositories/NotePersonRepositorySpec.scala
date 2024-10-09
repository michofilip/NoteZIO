package zote.db.repositories

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NotePersonEntity, PersonEntity}
import zote.db.repositories.NoteLabelRepositorySpec.test
import zote.enums.{NotePersonRole, NoteStatus}
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object NotePersonRepositorySpec extends ZIOSpecDefault {

  private val note1 = NoteEntity(
    title = "Note 1",
    message = "Message 1",
    status = NoteStatus.Ongoing,
    parentId = None
  )

  private val note2 = NoteEntity(
    title = "Note 2",
    message = "Message 2",
    status = NoteStatus.Ongoing,
    parentId = None
  )

  private val note3 = NoteEntity(
    title = "Note 3",
    message = "Message 3",
    status = NoteStatus.Ongoing,
    parentId = None
  )

  private val person1 = PersonEntity(name = "Ala")
  private val person2 = PersonEntity(name = "Ela")
  private val person3 = PersonEntity(name = "Ola")

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NotePersonRepository")(
      suite("provides function 'findAllByNoteId' that")(
        test("returns list of NotePersonEntities if some exist") {
          for {
            note <- DbHelper.insertNote(note1)
            person1 <- DbHelper.insertPerson(person1)
            person2 <- DbHelper.insertPerson(person2)
            person3 <- DbHelper.insertPerson(person3)
            notePerson1 <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note.id,
                personId = person1.id,
                role = NotePersonRole.Owner
              )
            )
            notePerson2 <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note.id,
                personId = person2.id,
                role = NotePersonRole.Maintainer
              )
            )
            notePerson3 <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note.id,
                personId = person3.id,
                role = NotePersonRole.Observer
              )
            )

            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            notePersonEntities.size == 3
            && notePersonEntities.contains(notePerson1)
            && notePersonEntities.contains(notePerson2)
            && notePersonEntities.contains(notePerson3)
          }
        },
        test("returns empty list if none exist") {
          for {
            note <- DbHelper.insertNote(note1)
            _ <- DbHelper.insertPerson(person1)
            _ <- DbHelper.insertPerson(person2)
            _ <- DbHelper.insertPerson(person3)
            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            notePersonEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findAllByPersonId' that")(
        test("returns list of NotePersonEntities if some exist") {
          for {
            note1 <- DbHelper.insertNote(note1)
            note2 <- DbHelper.insertNote(note2)
            note3 <- DbHelper.insertNote(note3)
            person <- DbHelper.insertPerson(person1)
            notePerson1 <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note1.id,
                personId = person.id,
                role = NotePersonRole.Owner
              )
            )
            notePerson2 <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note2.id,
                personId = person.id,
                role = NotePersonRole.Maintainer
              )
            )
            notePerson3 <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note3.id,
                personId = person.id,
                role = NotePersonRole.Observer
              )
            )

            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByPersonId(
              person.id
            )
          } yield assertTrue {
            notePersonEntities.size == 3
            && notePersonEntities.contains(notePerson1)
            && notePersonEntities.contains(notePerson2)
            && notePersonEntities.contains(notePerson3)
          }
        },
        test("returns empty list if none exist") {
          for {
            _ <- DbHelper.insertNote(note1)
            _ <- DbHelper.insertNote(note2)
            _ <- DbHelper.insertNote(note3)
            person <- DbHelper.insertPerson(person1)

            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByPersonId(
              person.id
            )
          } yield assertTrue {
            notePersonEntities.isEmpty
          }
        }
      ),
      suite("provides function 'insert' that")(
        test("inserts list of NotePersonEntities") {
          for {
            note <- DbHelper.insertNote(note1)
            person <- DbHelper.insertPerson(person1)
            notePerson = NotePersonEntity(
              noteId = note.id,
              personId = person.id,
              role = NotePersonRole.Owner
            )

            notePersonRepository <- ZIO.service[NotePersonRepository]
            _ <- notePersonRepository.insert(List(notePerson))

            notePersonEntitiesByNoteId <- notePersonRepository
              .findAllByNoteId(note.id)
            notePersonEntitiesByPersonId <- notePersonRepository
              .findAllByPersonId(person.id)
          } yield assertTrue {
            notePersonEntitiesByNoteId.contains(notePerson)
            && notePersonEntitiesByPersonId.contains(notePerson)
          }
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes list of NotePersonEntities") {
          for {
            note <- DbHelper.insertNote(note1)
            person <- DbHelper.insertPerson(person1)
            notePerson <- DbHelper.insertNotePerson(
              NotePersonEntity(
                noteId = note.id,
                personId = person.id,
                role = NotePersonRole.Owner
              )
            )

            notePersonRepository <- ZIO.service[NotePersonRepository]
            _ <- notePersonRepository.delete(
              List(
                NotePersonEntity(
                  noteId = note.id,
                  personId = person.id,
                  role = NotePersonRole.Owner
                )
              )
            )

            notePersonEntitiesByNoteId <- notePersonRepository
              .findAllByNoteId(note.id)
            notePersonEntitiesByPersonId <- notePersonRepository
              .findAllByPersonId(person.id)
          } yield assertTrue {
            notePersonEntitiesByNoteId.isEmpty
            && notePersonEntitiesByPersonId.isEmpty
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
    NotePersonRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

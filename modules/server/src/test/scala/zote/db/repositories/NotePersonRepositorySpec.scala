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

  private val roles = List(
    NotePersonRole.Owner,
    NotePersonRole.Maintainer,
    NotePersonRole.Observer
  )

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NotePersonRepository")(
      suite("provides function 'findAllByNoteId' that")(
        test("returns list of NotePersonEntities if present") {
          for {
            note <- DbHelper.insertNote(note1)
            persons <- DbHelper.insertPersons(List(person1, person2, person3))
            expected <- DbHelper.insertNotePersons(
              (persons zip roles).map { case (person, role) =>
                NotePersonEntity(
                  noteId = note.id,
                  personId = person.id,
                  role = role
                )
              }
            )
            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            notePersonEntities.size == expected.size
            && notePersonEntities.toSet == expected.toSet
          }
        },
        test("returns empty list if none") {
          for {
            note <- DbHelper.insertNote(note1)
            _ <- DbHelper.insertPersons(List(person1, person2, person3))
            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            notePersonEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findAllByPersonId' that")(
        test("returns list of NotePersonEntities if present") {
          for {
            notes <- DbHelper.insertNotes(List(note1, note2, note2))
            person <- DbHelper.insertPerson(person1)
            expected <- DbHelper.insertNotePersons(
              (notes zip roles).map { case (note, role) =>
                NotePersonEntity(
                  noteId = note.id,
                  personId = person.id,
                  role = role
                )
              }
            )
            notePersonRepository <- ZIO.service[NotePersonRepository]
            notePersonEntities <- notePersonRepository.findAllByPersonId(
              person.id
            )
          } yield assertTrue {
            notePersonEntities.size == expected.size
            && notePersonEntities.toSet == expected.toSet
          }
        },
        test("returns empty list if none") {
          for {
            _ <- DbHelper.insertNotes(List(note1, note2, note2))
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

            notePersonRepository <- ZIO.service[NotePersonRepository]
            isEmptyByNote <- notePersonRepository
              .findAllByNoteId(note.id)
              .map(_.isEmpty)
            isEmptyByLabel <- notePersonRepository
              .findAllByPersonId(person.id)
              .map(_.isEmpty)

            _ <- notePersonRepository.insert(
              List(
                NotePersonEntity(
                  noteId = note.id,
                  personId = person.id,
                  role = NotePersonRole.Owner
                )
              )
            )

            nonEmptyByNote <- notePersonRepository
              .findAllByNoteId(note.id)
              .map(_.nonEmpty)
            nonEmptyByLabel <- notePersonRepository
              .findAllByPersonId(person.id)
              .map(_.nonEmpty)
          } yield assertTrue {
            isEmptyByNote && isEmptyByLabel && nonEmptyByNote && nonEmptyByLabel
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
            nonEmptyByNote <- notePersonRepository
              .findAllByNoteId(note.id)
              .map(_.nonEmpty)
            nonEmptyByLabel <- notePersonRepository
              .findAllByPersonId(person.id)
              .map(_.nonEmpty)

            _ <- notePersonRepository.delete(
              List(
                NotePersonEntity(
                  noteId = note.id,
                  personId = person.id,
                  role = NotePersonRole.Owner
                )
              )
            )

            isEmptyByNote <- notePersonRepository
              .findAllByNoteId(note.id)
              .map(_.isEmpty)
            isEmptyByLabel <- notePersonRepository
              .findAllByPersonId(person.id)
              .map(_.isEmpty)
          } yield assertTrue {
            nonEmptyByNote && nonEmptyByLabel && isEmptyByNote && isEmptyByLabel
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

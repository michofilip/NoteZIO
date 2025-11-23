package zote.db.repositories

import zio.*
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zote.Ids.NoteId
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.NoteEntity
import zote.enums.NoteStatus
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object NoteRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteRepository")(
      suite("provides function 'findAll' that")(
        test("returns list of NoteEntities if some exist") {
          for {
            noteEntity1 <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteEntity2 <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 2",
                message = "Message 2",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )

            noteRepository <- ZIO.service[NoteRepository]
            noteEntities   <- noteRepository.findAll
          } yield assertTrue {
            noteEntities.size == 2 &&
            noteEntities.contains(noteEntity1) &&
            noteEntities.contains(noteEntity2)
          }
        },
        test("returns empty list if none exist") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            noteEntities   <- noteRepository.findAll
          } yield assertTrue {
            noteEntities.isEmpty
          }
        },
      ),
      suite("provides function 'findById' that")(
        test("returns option with NoteEntity if exists") {
          for {
            expected <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteRepository  <- ZIO.service[NoteRepository]
            maybeNoteEntity <- noteRepository.findById(expected.id)
          } yield assertTrue {
            maybeNoteEntity.contains(expected)
          }
        },
        test("returns empty option if not exists") {
          for {
            noteRepository  <- ZIO.service[NoteRepository]
            maybeNoteEntity <- noteRepository.findById(NoteId(-1))
          } yield assertTrue {
            maybeNoteEntity.isEmpty
          }
        },
      ),
      suite("provides function 'findAllByParentId' that")(
        test("returns list of NoteEntities if some exist") {
          for {
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            childNoteEntity1 <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 2",
                message = "Message 2",
                status = NoteStatus.Ongoing,
                parentId = Some(noteEntity.id),
              ),
            )
            childNoteEntity2 <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 3",
                message = "Message 3",
                status = NoteStatus.Ongoing,
                parentId = Some(noteEntity.id),
              ),
            )

            noteRepository <- ZIO.service[NoteRepository]
            noteEntities   <- noteRepository.findAllByParentNoteId(noteEntity.id)
          } yield assertTrue {
            noteEntities.size == 2 &&
            noteEntities.contains(childNoteEntity1) &&
            noteEntities.contains(childNoteEntity2)
          }
        },
        test("returns empty list if none exist") {
          for {
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteRepository <- ZIO.service[NoteRepository]
            noteEntities   <- noteRepository.findAllByParentNoteId(noteEntity.id)
          } yield assertTrue {
            noteEntities.isEmpty
          }
        },
      ),
      suite("provides function 'getById' that")(
        test("returns NoteEntity if exists") {
          for {
            note <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteRepository <- ZIO.service[NoteRepository]
            noteEntity     <- noteRepository.getById(note.id)
          } yield assertTrue {
            noteEntity == note
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            result         <- noteRepository.getById(NoteId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Note id: -1 not found"))
          }
        },
      ),
      suite("provides function 'upsert' that")(
        test("inserts and returns NoteEntity if not exists") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            noteEntity <- noteRepository.upsert(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
          } yield assertTrue {
            !noteEntity.id.isZero &&
            noteEntity.title == "Note 1"
          }
        },
        test("updates and returns NoteEntity if exists") {
          for {
            note <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteRepository <- ZIO.service[NoteRepository]
            noteEntity <- noteRepository.upsert(
              note.copy(title = "Better title"),
            )
          } yield assertTrue {
            noteEntity.id == note.id &&
            noteEntity.title == "Better title"
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes NoteEntity if exists") {
          for {
            note <- DbHelper
              .insertNote(
                NoteEntity(
                  title = "Note 1",
                  message = "Message 1",
                  status = NoteStatus.Ongoing,
                  parentId = None,
                ),
              )
            noteRepository              <- ZIO.service[NoteRepository]
            maybeNoteEntityBeforeDelete <- noteRepository.findById(note.id)
            _                           <- noteRepository.delete(note.id)
            maybeNoteEntityAfterDelete  <- noteRepository.findById(note.id)
          } yield assertTrue {
            maybeNoteEntityBeforeDelete.isDefined &&
            maybeNoteEntityAfterDelete.isEmpty
          }
        },
        test("does nothing if NoteEntity not exists") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            result         <- noteRepository.delete(NoteId(-1)).exit
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
    NoteRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

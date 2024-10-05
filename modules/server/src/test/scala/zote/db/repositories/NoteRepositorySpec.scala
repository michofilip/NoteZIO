package zote.db.repositories

import com.softwaremill.quicklens.modify
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.NoteEntity
import zote.db.repositories.PersonRepositorySpec.{suite, test}
import zote.enums.NoteStatus
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object NoteRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteRepository")(
      suite("provides function 'findAll' that")(
        test("returns list of NoteEntities if present") {
          for {
            expected <- DbHelper.insertNotes(
              List(
                NoteEntity(
                  title = "Note 1",
                  message = "Message 1",
                  status = NoteStatus.Ongoing,
                  parentId = None
                ),
                NoteEntity(
                  title = "Note 2",
                  message = "Message 2",
                  status = NoteStatus.Ongoing,
                  parentId = None
                )
              )
            )
            noteRepository <- ZIO.service[NoteRepository]
            noteEntities <- noteRepository.findAll
          } yield assertTrue {
            noteEntities.size == expected.size
            && noteEntities.toSet == expected.toSet
          }
        },
        test("returns empty list none exist") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            noteEntities <- noteRepository.findAll
          } yield assertTrue {
            noteEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findById' that")(
        test("returns option with NoteEntity if exists") {
          for {
            expected <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteRepository <- ZIO.service[NoteRepository]
            maybeNoteEntity <- noteRepository.findById(expected.id)
          } yield assertTrue(maybeNoteEntity.contains(expected))
        },
        test("returns empty option if not exists") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            maybeNoteEntity <- noteRepository.findById(-1)
          } yield assertTrue(maybeNoteEntity.isEmpty)
        }
      ),
      suite("provides function 'getById' that")(
        test("returns NoteEntity if exists") {
          for {
            expected <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteRepository <- ZIO.service[NoteRepository]
            noteEntity <- noteRepository.getById(expected.id)
          } yield assertTrue(noteEntity == expected)
        },
        test("returns NotFoundException if not exists") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            result <- noteRepository
              .getById(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Note id: -1 not found"
              case _ => false
          }
        }
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
                parentId = None
              )
            )
          } yield assertTrue(
            noteEntity.id != 0 && noteEntity.title == "Note 1"
          )
        },
        test("updates and returns NoteEntity if exists") {
          for {
            note <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteRepository <- ZIO.service[NoteRepository]
            noteEntity <- noteRepository.upsert(
              note.modify(_.title).setTo("Better title")
            )
          } yield assertTrue(
            noteEntity.id == note.id && noteEntity.title == "Better title"
          )
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes NoteEntity if exists") {
          for {
            id <- DbHelper
              .insertNote(
                NoteEntity(
                  title = "Note 1",
                  message = "Message 1",
                  status = NoteStatus.Ongoing,
                  parentId = None
                )
              )
              .map(_.id)
            noteRepository <- ZIO.service[NoteRepository]
            isDefined <- noteRepository
              .findById(id)
              .map(_.isDefined)
            _ <- noteRepository.delete(id)
            isEmpty <- noteRepository.findById(id).map(_.isEmpty)
          } yield assertTrue(isDefined && isEmpty)
        },
        test("does nothing if NoteEntity not exists") {
          for {
            noteRepository <- ZIO.service[NoteRepository]
            result <- noteRepository.delete(-1).fold(_ => false, _ => true)
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
    NoteRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

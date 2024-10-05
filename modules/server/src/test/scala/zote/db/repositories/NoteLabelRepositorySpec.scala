package zote.db.repositories

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{LabelEntity, NoteEntity, NoteLabelEntity}
import zote.db.repositories.PersonRepositorySpec.suite
import zote.enums.NoteStatus
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object NoteLabelRepositorySpec extends ZIOSpecDefault {
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

  private val label1 = LabelEntity(name = "Red")
  private val label2 = LabelEntity(name = "Green")

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteLabelRepository")(
      suite("provides function 'findAllByNoteId' that")(
        test("returns list of NoteLabelEntities if present") {
          for {
            note <- DbHelper.insertNote(note1)
            labels <- DbHelper.insertLabels(List(label1, label2))
            expected <- DbHelper.insertNoteLabels(
              labels.map { label =>
                NoteLabelEntity(noteId = note.id, labelId = label.id)
              }
            )
            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteLabelEntities.size == expected.size
            && noteLabelEntities.toSet == expected.toSet
          }
        },
        test("returns empty list if none") {
          for {
            note <- DbHelper.insertNote(note1)
            _ <- DbHelper.insertLabels(List(label1, label2))
            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteLabelEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findAllByLabelId' that")(
        test("returns list of NoteLabelEntities if present") {
          for {
            notes <- DbHelper.insertNotes(List(note1, note2))
            label <- DbHelper.insertLabel(label1)
            expected <- DbHelper.insertNoteLabels(
              notes.map { note =>
                NoteLabelEntity(noteId = note.id, labelId = label.id)
              }
            )
            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntities.size == expected.size
            && noteLabelEntities.toSet == expected.toSet
          }
        },
        test("returns empty list if none") {
          for {
            _ <- DbHelper.insertNotes(List(note1, note2))
            label <- DbHelper.insertLabel(label1)
            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntities.isEmpty
          }
        }
      ),
      suite("provides function 'insert' that")(
        test("inserts list of NoteLabelEntities") {
          for {
            note <- DbHelper.insertNote(note1)
            label <- DbHelper.insertLabel(label1)

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            isEmptyByNote <- noteLabelRepository
              .findAllByNoteId(note.id)
              .map(_.isEmpty)
            isEmptyByLabel <- noteLabelRepository
              .findAllByLabelId(label.id)
              .map(_.isEmpty)

            _ <- noteLabelRepository.insert(
              List(NoteLabelEntity(noteId = note.id, labelId = label.id))
            )

            nonEmptyByNote <- noteLabelRepository
              .findAllByNoteId(note.id)
              .map(_.nonEmpty)
            nonEmptyByLabel <- noteLabelRepository
              .findAllByLabelId(label.id)
              .map(_.nonEmpty)
          } yield assertTrue {
            isEmptyByNote && isEmptyByLabel && nonEmptyByNote && nonEmptyByLabel
          }
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes list of NoteLabelEntities") {
          for {
            note <- DbHelper.insertNote(note1)
            label <- DbHelper.insertLabel(label1)
            noteLabel <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = note.id, labelId = label.id)
            )

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            nonEmptyByNote <- noteLabelRepository
              .findAllByNoteId(note.id)
              .map(_.nonEmpty)
            nonEmptyByLabel <- noteLabelRepository
              .findAllByLabelId(label.id)
              .map(_.nonEmpty)

            _ <- noteLabelRepository.delete(
              List(noteLabel)
            )

            isEmptyByNote <- noteLabelRepository
              .findAllByNoteId(note.id)
              .map(_.isEmpty)
            isEmptyByLabel <- noteLabelRepository
              .findAllByLabelId(label.id)
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
    NoteLabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

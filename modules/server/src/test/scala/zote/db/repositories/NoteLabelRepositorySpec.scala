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
        test("returns list of NoteLabelEntities if some exist") {
          for {
            note <- DbHelper.insertNote(note1)
            label1 <- DbHelper.insertLabel(label1)
            label2 <- DbHelper.insertLabel(label2)
            noteLabel1 <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = note.id, labelId = label1.id)
            )
            noteLabel2 <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = note.id, labelId = label2.id)
            )

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteLabelEntities.size == 2
            && noteLabelEntities.contains(noteLabel1)
            && noteLabelEntities.contains(noteLabel2)
          }
        },
        test("returns empty list if none exist") {
          for {
            note <- DbHelper.insertNote(note1)
            _ <- DbHelper.insertLabel(label1)
            _ <- DbHelper.insertLabel(label2)

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteLabelEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findAllByLabelId' that")(
        test("returns list of NoteLabelEntities if some exist") {
          for {
            note1 <- DbHelper.insertNote(note1)
            note2 <- DbHelper.insertNote(note2)
            label <- DbHelper.insertLabel(label1)
            noteLabel1 <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = note1.id, labelId = label.id)
            )
            noteLabel2 <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = note2.id, labelId = label.id)
            )

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntities.size == 2
            && noteLabelEntities.contains(noteLabel1)
            && noteLabelEntities.contains(noteLabel2)
          }
        },
        test("returns empty list if none exist") {
          for {
            _ <- DbHelper.insertNote(note1)
            _ <- DbHelper.insertNote(note2)
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
            noteLabel = NoteLabelEntity(noteId = note.id, labelId = label.id)

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            _ <- noteLabelRepository.insert(
              List(noteLabel)
            )

            noteLabelEntitiesByNoteId <- noteLabelRepository.findAllByNoteId(
              note.id
            )
            noteLabelEntitiesByLabelId <- noteLabelRepository.findAllByLabelId(
              label.id
            )
          } yield assertTrue {
            noteLabelEntitiesByNoteId.size == 1
            && noteLabelEntitiesByLabelId.size == 1
            && noteLabelEntitiesByNoteId.contains(noteLabel)
            && noteLabelEntitiesByLabelId.contains(noteLabel)
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
            _ <- noteLabelRepository.delete(
              List(noteLabel)
            )

            noteLabelEntitiesByNoteId <- noteLabelRepository
              .findAllByNoteId(note.id)
            noteLabelEntitiesByLabelId <- noteLabelRepository
              .findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntitiesByNoteId.isEmpty
            && noteLabelEntitiesByLabelId.isEmpty
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

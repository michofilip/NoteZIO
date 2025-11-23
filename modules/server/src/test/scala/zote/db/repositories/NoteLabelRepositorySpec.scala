package zote.db.repositories

import zio.*
import zio.test.*
import zote.Ids.{LabelId, NoteId}
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{LabelEntity, NoteEntity, NoteLabelEntity}
import zote.enums.NoteStatus
import zote.helpers.{DbHelper, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object NoteLabelRepositorySpec extends ZIOSpecDefault {
  private val note1 = NoteEntity(
    title = "Note 1",
    message = "Message 1",
    status = NoteStatus.Ongoing,
    parentId = None,
  )

  private val note2 = NoteEntity(
    title = "Note 2",
    message = "Message 2",
    status = NoteStatus.Ongoing,
    parentId = None,
  )

  private val label1 = LabelEntity(name = "Red")
  private val label2 = LabelEntity(name = "Green")

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("NoteLabelRepository")(
      suite("provides function 'findAllByNoteId' that")(
        test("returns list of NoteLabelEntities if some exist") {
          for {
            note       <- DbHelper.insertNote(note1)
            label1     <- DbHelper.insertLabel(label1)
            label2     <- DbHelper.insertLabel(label2)
            noteLabel1 <- DbHelper.insertNoteLabel(NoteLabelEntity(noteId = note.id, labelId = label1.id))
            noteLabel2 <- DbHelper.insertNoteLabel(NoteLabelEntity(noteId = note.id, labelId = label2.id))

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities   <- noteLabelRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteLabelEntities.size == 2 &&
            noteLabelEntities.contains(noteLabel1) &&
            noteLabelEntities.contains(noteLabel2)
          }
        },
        test("returns empty list if none exist") {
          for {
            note <- DbHelper.insertNote(note1)

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities   <- noteLabelRepository.findAllByNoteId(note.id)
          } yield assertTrue {
            noteLabelEntities.isEmpty
          }
        },
      ),
      suite("provides function 'findAllByLabelId' that")(
        test("returns list of NoteLabelEntities if some exist") {
          for {
            note1      <- DbHelper.insertNote(note1)
            note2      <- DbHelper.insertNote(note2)
            label      <- DbHelper.insertLabel(label1)
            noteLabel1 <- DbHelper.insertNoteLabel(NoteLabelEntity(noteId = note1.id, labelId = label.id))
            noteLabel2 <- DbHelper.insertNoteLabel(NoteLabelEntity(noteId = note2.id, labelId = label.id))

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities   <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntities.size == 2 &&
            noteLabelEntities.contains(noteLabel1) &&
            noteLabelEntities.contains(noteLabel2)
          }
        },
        test("returns empty list if none exist") {
          for {
            label <- DbHelper.insertLabel(label1)

            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            noteLabelEntities   <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntities.isEmpty
          }
        },
      ),
      suite("provides function 'insertAll' that")(
        test("inserts list of NoteLabelEntities") {
          for {
            note  <- DbHelper.insertNote(note1)
            label <- DbHelper.insertLabel(label1)
            noteLabel = NoteLabelEntity(noteId = note.id, labelId = label.id)

            noteLabelRepository        <- ZIO.service[NoteLabelRepository]
            _                          <- noteLabelRepository.insertAll(List(noteLabel))
            noteLabelEntitiesByNoteId  <- noteLabelRepository.findAllByNoteId(note.id)
            noteLabelEntitiesByLabelId <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntitiesByNoteId.size == 1 &&
            noteLabelEntitiesByNoteId.contains(noteLabel) &&
            noteLabelEntitiesByLabelId.size == 1 &&
            noteLabelEntitiesByLabelId.contains(noteLabel)
          }
        },
      ),
      suite("provides function 'deleteAll' that")(
        test("deletes list of NoteLabelEntities if exist") {
          for {
            note      <- DbHelper.insertNote(note1)
            label     <- DbHelper.insertLabel(label1)
            noteLabel <- DbHelper.insertNoteLabel(NoteLabelEntity(noteId = note.id, labelId = label.id))

            noteLabelRepository                    <- ZIO.service[NoteLabelRepository]
            noteLabelEntitiesByNoteIdBeforeDelete  <- noteLabelRepository.findAllByNoteId(note.id)
            noteLabelEntitiesByLabelIdBeforeDelete <- noteLabelRepository.findAllByLabelId(label.id)
            _                                      <- noteLabelRepository.deleteAll(List((note.id, label.id)))
            noteLabelEntitiesByNoteIdAfterDelete   <- noteLabelRepository.findAllByNoteId(note.id)
            noteLabelEntitiesByLabelIdAfterDelete  <- noteLabelRepository.findAllByLabelId(label.id)
          } yield assertTrue {
            noteLabelEntitiesByNoteIdBeforeDelete.nonEmpty &&
            noteLabelEntitiesByNoteIdAfterDelete.isEmpty &&
            noteLabelEntitiesByLabelIdBeforeDelete.nonEmpty &&
            noteLabelEntitiesByLabelIdAfterDelete.isEmpty
          }
        },
        test("does nothing if NoteLabelEntities not exist") {
          for {
            noteLabelRepository <- ZIO.service[NoteLabelRepository]
            result              <- noteLabelRepository.deleteAll(List((NoteId(-1), LabelId(-1)))).exit
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
    NoteLabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

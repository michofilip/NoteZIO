package zote.services

import zio.*
import zio.test.*
import zote.Ids.LabelId
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{LabelEntity, NoteEntity, NoteLabelEntity}
import zote.db.repositories.{LabelRepositoryImpl, NoteLabelRepositoryImpl}
import zote.dto.Label
import zote.dto.form.LabelForm
import zote.enums.NoteStatus
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, TestAspectUtils}

object LabelServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("LabelService")(
      suite("provides function 'getAll' that")(
        test("returns list of Labels if some exist") {
          for {
            labelEntity1 <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelEntity2 <- DbHelper.insertLabel(LabelEntity(name = "Green"))

            labelService <- ZIO.service[LabelService]
            labels       <- labelService.getAll
          } yield assertTrue {
            labels.size == 2 &&
            labels.contains(Label(id = labelEntity1.id, name = labelEntity1.name)) &&
            labels.contains(Label(id = labelEntity2.id, name = labelEntity2.name))
          }
        },
        test("returns empty list if none exist") {
          for {
            labelService <- ZIO.service[LabelService]
            labels       <- labelService.getAll
          } yield assertTrue {
            labels.isEmpty
          }
        },
      ),
      suite("provides function 'getById' that")(
        test("returns Label if exists") {
          for {
            labelEntity  <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelService <- ZIO.service[LabelService]
            label        <- labelService.getById(labelEntity.id)
          } yield assertTrue {
            label.id == labelEntity.id &&
            label.name == labelEntity.name
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result       <- labelService.getById(LabelId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Label id: -1 not found"))
          }
        },
      ),
      suite("provides function 'create' that")(
        test("creates and returns Label") {
          for {
            labelService <- ZIO.service[LabelService]
            label        <- labelService.create(LabelForm(name = "Red"))
          } yield assertTrue {
            !label.id.isZero &&
            label.name == "Red"
          }
        },
      ),
      suite("provides function 'update' that")(
        test("updates and returns Label") {
          for {
            labelEntity  <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelService <- ZIO.service[LabelService]
            label <- labelService.update(
              labelEntity.id,
              LabelForm(name = "Green"),
            )
          } yield assertTrue {
            label.id == labelEntity.id &&
            label.name == "Green"
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result       <- labelService.update(LabelId(-1), LabelForm(name = "Red")).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Label id: -1 not found"))
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes Label") {
          for {
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "title",
                message = Some("message"),
                status = NoteStatus.Ongoing,
                parentId = None,
              ),
            )
            noteLabelEntity <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = noteEntity.id, labelId = labelEntity.id),
            )

            labelService       <- ZIO.service[LabelService]
            resultBeforeDelete <- labelService.getById(labelEntity.id).exit
            _                  <- labelService.delete(labelEntity.id)
            resultAfterDelete  <- labelService.getById(labelEntity.id).exit
          } yield assertTrue {
            resultBeforeDelete.isSuccess &&
            resultAfterDelete.isFailure
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result       <- labelService.delete(LabelId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("Label id: -1 not found"))
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
    LabelServiceImpl.layer,
    LabelRepositoryImpl.layer,
    NoteLabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

package zote.services

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{LabelEntity, NoteEntity, NoteLabelEntity}
import zote.db.repositories.{LabelRepositoryImpl, NoteLabelRepositoryImpl}
import zote.dto.form.LabelForm
import zote.enums.NoteStatus
import zote.exceptions.{NotFoundException, ValidationException}
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}

object LabelServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("LabelService")(
      suite("provides function 'getAll' that")(
        test("returns list of Labels if some exist") {
          for {
            labelEntities <- DbHelper.insertLabels(
              List(
                LabelEntity(name = "Red"),
                LabelEntity(name = "Green"),
                LabelEntity(name = "Blue")
              )
            )
            labelService <- ZIO.service[LabelService]
            labels <- labelService.getAll
          } yield assertTrue {
            labels.size == labelEntities.size
            && labels.forall(label =>
              labelEntities
                .find(l => l.id == label.id)
                .exists(l => l.name == label.name)
            )
          }
        },
        test("returns empty list if none exist") {
          for {
            labelService <- ZIO.service[LabelService]
            labels <- labelService.getAll
          } yield assertTrue {
            labels.isEmpty
          }
        }
      ),
      suite("provides function 'getById' that")(
        test("returns Label if exists") {
          for {
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelService <- ZIO.service[LabelService]
            label <- labelService.getById(labelEntity.id)
          } yield assertTrue(label.name == labelEntity.name)
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result <- labelService
              .getById(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Label id: -1 not found"
              case _ => false
          }
        }
      ),
      suite("provides function 'create' that")(
        test("creates and returns Label") {
          for {
            labelService <- ZIO.service[LabelService]
            label <- labelService.create(LabelForm(name = "Red"))
          } yield assertTrue(label.name == "Red")
        }
      ),
      suite("provides function 'update' that")(
        test("updates and returns Label") {
          for {
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelService <- ZIO.service[LabelService]
            label <- labelService.update(
              labelEntity.id,
              LabelForm(name = "Redder")
            )
          } yield assertTrue(label.name == "Redder")
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result <- labelService
              .update(-1, LabelForm(name = "Redder"))
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Label id: -1 not found"
              case _ => false
          }
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes Label") {
          for {
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelService <- ZIO.service[LabelService]
            _ <- labelService.delete(labelEntity.id, force = false)
            result <- labelService
              .getById(labelEntity.id)
              .fold(_ => true, _ => false)
          } yield assertTrue(result)
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result <- labelService
              .delete(-1, force = false)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Label id: -1 not found"
              case _ => false
          }
        },
        test("returns ValidationException if in use") {
          for {
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteLabelEntity <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = noteEntity.id, labelId = labelEntity.id)
            )

            labelService <- ZIO.service[LabelService]
            result <- labelService
              .delete(labelEntity.id, force = false)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: ValidationException =>
                e.getMessage == s"Label id: ${labelEntity.id} can not be deleted"
              case _ => false
          }
        },
        test("force deletes Label if in use") {
          for {
            labelEntity <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            noteEntity <- DbHelper.insertNote(
              NoteEntity(
                title = "Note 1",
                message = "Message 1",
                status = NoteStatus.Ongoing,
                parentId = None
              )
            )
            noteLabelEntity <- DbHelper.insertNoteLabel(
              NoteLabelEntity(noteId = noteEntity.id, labelId = labelEntity.id)
            )

            labelService <- ZIO.service[LabelService]
            _ <- labelService.delete(labelEntity.id, force = true)
            result <- labelService
              .getById(labelEntity.id)
              .fold(_ => true, _ => false)
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
    LabelServiceImpl.layer,
    LabelRepositoryImpl.layer,
    NoteLabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

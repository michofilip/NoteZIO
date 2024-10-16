package zote.services

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.{LabelEntity, NoteEntity, NoteLabelEntity}
import zote.db.repositories.{LabelRepositoryImpl, NoteLabelRepositoryImpl}
import zote.dto.Label
import zote.dto.form.LabelForm
import zote.enums.NoteStatus
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}

object LabelServiceSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("LabelService")(
      suite("provides function 'getAll' that")(
        test("returns list of Labels if some exist") {
          for {
            labelEntity1 <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelEntity2 <- DbHelper.insertLabel(LabelEntity(name = "Green"))

            labelService <- ZIO.service[LabelService]
            labels <- labelService.getAll
          } yield assertTrue {
            labels.size == 2
            && labels.contains(
              Label(id = labelEntity1.id, name = labelEntity1.name)
            )
            && labels.contains(
              Label(id = labelEntity2.id, name = labelEntity2.name)
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
            _ <- labelService.delete(labelEntity.id)
            result <- labelService
              .getById(labelEntity.id)
              .fold(_ => true, _ => false)
          } yield assertTrue(result)
        },
        test("returns NotFoundException if not exists") {
          for {
            labelService <- ZIO.service[LabelService]
            result <- labelService
              .delete(-1)
              .flip
              .orElseFail(new Throwable())
          } yield assertTrue {
            result match
              case e: NotFoundException =>
                e.getMessage == "Label id: -1 not found"
              case _ => false
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
    LabelServiceImpl.layer,
    LabelRepositoryImpl.layer,
    NoteLabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

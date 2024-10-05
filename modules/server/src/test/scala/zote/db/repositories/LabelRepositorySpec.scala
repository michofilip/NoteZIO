package zote.db.repositories

import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.LabelEntity
import zote.db.repositories.PersonRepositorySpec.{suite, test}
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, DbHelperImpl, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object LabelRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("LabelRepository")(
      suite("provides function 'findAll' that")(
        test("returns list of LabelEntities if present") {
          for {
            expected <- DbHelper.insertLabels(
              List(
                LabelEntity(name = "Red"),
                LabelEntity(name = "Green"),
                LabelEntity(name = "Blue")
              )
            )
            labelRepository <- ZIO.service[LabelRepository]
            labelEntities <- labelRepository.findAll
          } yield assertTrue {
            labelEntities.size == expected.size
            && labelEntities.toSet == expected.toSet
          }
        },
        test("returns empty list none exist") {
          for {
            labelRepository <- ZIO.service[LabelRepository]
            labelEntities <- labelRepository.findAll
          } yield assertTrue {
            labelEntities.isEmpty
          }
        }
      ),
      suite("provides function 'findById' that")(
        test("returns option with LabelEntity if exists") {
          for {
            expected <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelRepository <- ZIO.service[LabelRepository]
            maybeLabelEntity <- labelRepository.findById(expected.id)
          } yield assertTrue(maybeLabelEntity.contains(expected))
        },
        test("returns empty option if not exists") {
          for {
            labelRepository <- ZIO.service[LabelRepository]
            maybeLabelEntity <- labelRepository.findById(-1)
          } yield assertTrue(maybeLabelEntity.isEmpty)
        }
      ),
      suite("provides function 'getById' that")(
        test("returns LabelEntity if exists") {
          for {
            expected <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelRepository <- ZIO.service[LabelRepository]
            labelEntity <- labelRepository.getById(expected.id)
          } yield assertTrue(labelEntity == expected)
        },
        test("returns NotFoundException if not exists") {
          for {
            labelRepository <- ZIO.service[LabelRepository]
            result <- labelRepository
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
      suite("provides function 'upsert' that")(
        test("inserts and returns LabelEntity if not exists") {
          for {
            labelRepository <- ZIO.service[LabelRepository]
            labelEntity <- labelRepository.upsert(LabelEntity(name = "Yellow"))
          } yield assertTrue(
            labelEntity.id != 0 && labelEntity.name == "Yellow"
          )
        },
        test("updates and returns LabelEntity if exists") {
          for {
            label <- DbHelper.insertLabel(LabelEntity(name = "Red"))
            labelRepository <- ZIO.service[LabelRepository]
            labelEntity <- labelRepository.upsert(label.copy(name = "Redder"))
          } yield assertTrue(
            labelEntity.id == label.id && labelEntity.name == "Redder"
          )
        }
      ),
      suite("provides function 'delete' that")(
        test("deletes LabelEntity if exists") {
          for {
            id <- DbHelper.insertLabel(LabelEntity(name = "Red")).map(_.id)
            labelRepository <- ZIO.service[LabelRepository]
            isDefined <- labelRepository
              .findById(id)
              .map(_.isDefined)
            _ <- labelRepository.delete(id)
            isEmpty <- labelRepository.findById(id).map(_.isEmpty)
          } yield assertTrue(isDefined && isEmpty)
        },
        test("does nothing if LabelEntity not exists") {
          for {
            labelRepository <- ZIO.service[LabelRepository]
            result <- labelRepository.delete(-1).fold(_ => false, _ => true)
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
    LabelRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelperImpl.layer
  )
}

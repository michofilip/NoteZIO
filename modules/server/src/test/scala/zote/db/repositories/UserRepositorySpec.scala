package zote.db.repositories

import zio.*
import zio.test.*
import zote.Ids.UserId
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.UserEntity
import zote.exceptions.NotFoundException
import zote.helpers.{DbHelper, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object UserRepositorySpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("UserRepository")(
      suite("provides function 'findAll' that")(
        test("returns list of UserEntities if some exist") {
          for {
            user1 <- DbHelper.insertUser(UserEntity(name = "Ala"))
            user2 <- DbHelper.insertUser(UserEntity(name = "Ela"))

            userRepository <- ZIO.service[UserRepository]
            userEntities   <- userRepository.findAll
          } yield assertTrue {
            userEntities.size == 2 &&
            userEntities.contains(user1) &&
            userEntities.contains(user2)
          }
        },
        test("returns empty list if none exist") {
          for {
            userRepository <- ZIO.service[UserRepository]
            userEntities   <- userRepository.findAll
          } yield assertTrue {
            userEntities.isEmpty
          }
        },
      ),
      suite("provides function 'findById' that")(
        test("returns option with UserEntity if exists") {
          for {
            user            <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userRepository  <- ZIO.service[UserRepository]
            maybeUserEntity <- userRepository.findById(user.id)
          } yield assertTrue {
            maybeUserEntity.contains(user)
          }
        },
        test("returns empty option if not exists") {
          for {
            userRepository  <- ZIO.service[UserRepository]
            maybeUserEntity <- userRepository.findById(UserId(-1))
          } yield assertTrue {
            maybeUserEntity.isEmpty
          }
        },
      ),
      suite("provides function 'getById' that")(
        test("returns UserEntity if exists") {
          for {
            user           <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userRepository <- ZIO.service[UserRepository]
            userEntity     <- userRepository.getById(user.id)
          } yield assertTrue {
            userEntity == user
          }
        },
        test("returns NotFoundException if not exists") {
          for {
            userRepository <- ZIO.service[UserRepository]
            result           <- userRepository.getById(UserId(-1)).exit
          } yield assertTrue {
            result == Exit.fail(NotFoundException("User id: -1 not found"))
          }
        },
      ),
      suite("provides function 'findByName' that")(
        test("returns option with UserEntity if exists") {
          for {
            user            <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userRepository  <- ZIO.service[UserRepository]
            maybeUserEntity <- userRepository.findByName(user.name)
          } yield assertTrue {
            maybeUserEntity.contains(user)
          }
        },
        test("returns empty option if not exists") {
          for {
            userRepository  <- ZIO.service[UserRepository]
            maybeUserEntity <- userRepository.findByName("Ala")
          } yield assertTrue {
            maybeUserEntity.isEmpty
          }
        },
      ),
      suite("provides function 'upsert' that")(
        test("inserts and returns UserEntity if not exists") {
          for {
            userRepository <- ZIO.service[UserRepository]
            userEntity     <- userRepository.upsert(UserEntity(name = "Ela"))
          } yield assertTrue {
            !userEntity.id.isZero &&
            userEntity.name == "Ela"
          }
        },
        test("updates and returns UserEntity if exists") {
          for {
            user           <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userRepository <- ZIO.service[UserRepository]
            userEntity     <- userRepository.upsert(user.copy(name = "Ela"))
          } yield assertTrue {
            userEntity.id == user.id &&
            userEntity.name == "Ela"
          }
        },
      ),
      suite("provides function 'delete' that")(
        test("deletes UserEntity if exists") {
          for {
            user                        <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userRepository              <- ZIO.service[UserRepository]
            maybeUserEntityBeforeDelete <- userRepository.findById(user.id)
            _                             <- userRepository.delete(user.id)
            maybeUserEntityAfterDelete  <- userRepository.findById(user.id)
          } yield assertTrue {
            maybeUserEntityBeforeDelete.isDefined &&
            maybeUserEntityAfterDelete.isEmpty
          }
        },
        test("does nothing if UserEntity not exists") {
          for {
            userRepository <- ZIO.service[UserRepository]
            result           <- userRepository.delete(UserId(-1)).exit
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
    UserRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

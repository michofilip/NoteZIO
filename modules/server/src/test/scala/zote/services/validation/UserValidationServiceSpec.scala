package zote.services.validation

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.UserEntity
import zote.db.repositories.UserRepositoryImpl
import zote.dto.form.UserForm
import zote.exceptions.ValidationException
import zote.helpers.{DbHelper, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object UserValidationServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("UserValidationService")(
      suite("provides function 'validateForCreate' that")(
        test("returns valid UserForm if correct") {
          val userFormRaw =
            UserForm.Raw(
              name = Some("Ala"),
            )

          for {
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForCreate(userFormRaw).exit
          } yield assertTrue {
            result == Exit.succeed {
              UserForm(
                name = "Ala",
              )
            }
          }
        },
        test("returns ValidationException if name is missing") {
          val userFormRaw =
            UserForm.Raw(
              name = None,
            )

          for {
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForCreate(userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name is required"))
            }
          }
        },
        test("returns ValidationException if name is too short") {
          val userFormRaw =
            UserForm.Raw(
              name = Some("XX"),
            )

          for {
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForCreate(userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be longer or equal to 3"))
            }
          }
        },
        test("returns ValidationException if name is too long") {
          val userFormRaw =
            UserForm.Raw(
              name = Some(List.fill(256)("X").mkString),
            )

          for {
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForCreate(userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be shorter or equal to 255"))
            }
          }
        },
        test("returns ValidationException if name already exists") {
          val userFormRaw =
            UserForm.Raw(
              name = Some("Ala"),
            )

          for {
            _                       <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForCreate(userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name Ala already exists"))
            }
          }
        },
      ),
      suite("provides function 'validateForUpdate' that")(
        test("returns valid UserForm if correct") {
          val userFormRaw =
            UserForm.Raw(
              name = Some("Ela"),
            )

          for {
            user                  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForUpdate(user.id, userFormRaw).exit
          } yield assertTrue {
            result == Exit.succeed {
              UserForm(
                name = "Ela",
              )
            }
          }
        },
        test("returns ValidationException if name is missing") {
          val userFormRaw =
            UserForm.Raw(
              name = None,
            )

          for {
            user                  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForUpdate(user.id, userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name is required"))
            }
          }
        },
        test("returns ValidationException if name is too short") {
          val userFormRaw =
            UserForm.Raw(
              name = Some("XX"),
            )

          for {
            user                  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForUpdate(user.id, userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be longer or equal to 3"))
            }
          }
        },
        test("returns ValidationException if name is too long") {
          val userFormRaw =
            UserForm.Raw(
              name = Some(List.fill(256)("X").mkString),
            )

          for {
            user                  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForUpdate(user.id, userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be shorter or equal to 255"))
            }
          }
        },
        test("returns ValidationException if name already exists") {
          val userFormRaw =
            UserForm.Raw(
              name = Some("Ela"),
            )

          for {
            user                  <- DbHelper.insertUser(UserEntity(name = "Ala"))
            _                       <- DbHelper.insertUser(UserEntity(name = "Ela"))
            userValidationService <- ZIO.service[UserValidationServiceImpl]
            result                  <- userValidationService.validateForUpdate(user.id, userFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name Ela already exists"))
            }
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
    UserValidationServiceImpl.layer,
    UserRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

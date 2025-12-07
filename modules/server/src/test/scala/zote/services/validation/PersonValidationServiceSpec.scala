package zote.services.validation

import zio.*
import zio.test.*
import zote.config.{DataSourceConfig, FlywayConfig}
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.db.repositories.PersonRepositoryImpl
import zote.dto.form.PersonForm
import zote.exceptions.ValidationException
import zote.helpers.{DbHelper, TestAspectUtils}
import zote.services.{FlywayService, FlywayServiceImpl}

object PersonValidationServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("PersonValidationService")(
      suite("provides function 'validateForCreate' that")(
        test("returns valid PersonForm if correct") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some("Ala"),
            )

          for {
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForCreate(personFormRaw).exit
          } yield assertTrue {
            result == Exit.succeed {
              PersonForm(
                name = "Ala",
              )
            }
          }
        },
        test("returns ValidationException if name is missing") {
          val personFormRaw =
            PersonForm.Raw(
              name = None,
            )

          for {
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForCreate(personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name is required"))
            }
          }
        },
        test("returns ValidationException if name is too short") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some("XX"),
            )

          for {
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForCreate(personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be longer or equal to 3"))
            }
          }
        },
        test("returns ValidationException if name is too long") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some(List.fill(256)("X").mkString),
            )

          for {
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForCreate(personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be shorter or equal to 255"))
            }
          }
        },
        test("returns ValidationException if name already exists") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some("Ala"),
            )

          for {
            _                       <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForCreate(personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name Ala already exists"))
            }
          }
        },
      ),
      suite("provides function 'validateForUpdate' that")(
        test("returns valid PersonForm if correct") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some("Ela"),
            )

          for {
            person                  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForUpdate(person.id, personFormRaw).exit
          } yield assertTrue {
            result == Exit.succeed {
              PersonForm(
                name = "Ela",
              )
            }
          }
        },
        test("returns ValidationException if name is missing") {
          val personFormRaw =
            PersonForm.Raw(
              name = None,
            )

          for {
            person                  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForUpdate(person.id, personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name is required"))
            }
          }
        },
        test("returns ValidationException if name is too short") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some("XX"),
            )

          for {
            person                  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForUpdate(person.id, personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be longer or equal to 3"))
            }
          }
        },
        test("returns ValidationException if name is too long") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some(List.fill(256)("X").mkString),
            )

          for {
            person                  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForUpdate(person.id, personFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be shorter or equal to 255"))
            }
          }
        },
        test("returns ValidationException if name already exists") {
          val personFormRaw =
            PersonForm.Raw(
              name = Some("Ela"),
            )

          for {
            person                  <- DbHelper.insertPerson(PersonEntity(name = "Ala"))
            _                       <- DbHelper.insertPerson(PersonEntity(name = "Ela"))
            personValidationService <- ZIO.service[PersonValidationServiceImpl]
            result                  <- personValidationService.validateForUpdate(person.id, personFormRaw).exit
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
    PersonValidationServiceImpl.layer,
    PersonRepositoryImpl.layer,
    QuillContext.layer,
    DataSourceConfig.layer,
    DbHelper.layer,
  )
}

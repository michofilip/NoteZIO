package zote.services.validation

import zio.*
import zio.test.*
import zote.dto.form.LabelForm
import zote.exceptions.ValidationException

object LabelValidationServiceSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("LabelValidationService")(
      suite("provides function 'validate' that")(
        test("returns valid LabelForm if correct") {
          val labelFormRaw =
            LabelForm.Raw(
              name = Some("Red"),
            )

          for {
            labelValidationService <- ZIO.service[LabelValidationService]
            result                 <- labelValidationService.validate(labelFormRaw).exit
          } yield assertTrue {
            result == Exit.succeed {
              LabelForm(
                name = "Red",
              )
            }
          }
        },
        test("returns ValidationException if name is missing") {
          val labelFormRaw =
            LabelForm.Raw(
              name = None,
            )

          for {
            labelValidationService <- ZIO.service[LabelValidationService]
            result                 <- labelValidationService.validate(labelFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name is required"))
            }
          }
        },
        test("returns ValidationException if name is blank") {
          val labelFormRaw =
            LabelForm.Raw(
              name = Some("   "),
            )

          for {
            labelValidationService <- ZIO.service[LabelValidationService]
            result                 <- labelValidationService.validate(labelFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must not be blank"))
            }
          }
        },
        test("returns ValidationException if name is too long") {
          val labelFormRaw =
            LabelForm.Raw(
              name = Some(List.fill(51)("x").mkString),
            )

          for {
            labelValidationService <- ZIO.service[LabelValidationService]
            result                 <- labelValidationService.validate(labelFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages = Set("name must be shorter or equal to 50"))
            }
          }
        },
        test("returns ValidationException with many messages if incorrect in many ways") {
          val labelFormRaw =
            LabelForm.Raw(
              name = Some(List.fill(51)(" ").mkString),
            )

          for {
            labelValidationService <- ZIO.service[LabelValidationService]
            result                 <- labelValidationService.validate(labelFormRaw).exit
          } yield assertTrue {
            result == Exit.fail {
              ValidationException(messages =
                Set(
                  "name must not be blank",
                  "name must be shorter or equal to 50",
                ),
              )
            }
          }
        },
      ),
    ) @@ TestAspect.sequential
  }.provide(
    LabelValidationServiceImpl.layer,
  )
}

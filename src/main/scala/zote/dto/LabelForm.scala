package zote.dto

import zio.*
import zio.json.JsonCodec
import zio.prelude.*
import zote.exceptions.ValidationException

case class LabelForm(
  name: String
)derives JsonCodec

object LabelForm {
  def validateZIO(labelForm: LabelForm): IO[ValidationException, Unit] =
    validate(labelForm).mapError(ValidationException.apply).toZIO

  private def validate(labelForm: LabelForm): Validation[String, Unit] =
    ZValidation.validateAll {
      Seq(
        validateNameNotBlank(labelForm),
        validateNameNotToLong(labelForm)
      )
    }.unit

  private def validateNameNotBlank(labelForm: LabelForm) =
    ZValidation.fromPredicateWith("Name cannot be blank")(labelForm)(labelForm => !labelForm.name.isBlank)

  private def validateNameNotToLong(labelForm: LabelForm) =
    ZValidation.fromPredicateWith("Name cannot be longer then 255 characters")(labelForm)(labelForm => labelForm.name.length <= 255)
}

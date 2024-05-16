package zote.dto

import zio.*
import zio.json.JsonCodec
import zio.prelude.*
import zote.exceptions.ValidationException

case class PersonForm(
  name: String
)derives JsonCodec

object PersonForm {
  def validateZIO(personForm: PersonForm): IO[ValidationException, Unit] =
    validate(personForm).mapError(ValidationException.apply).toZIO

  private def validate(personForm: PersonForm): Validation[String, Unit] =
    ZValidation.validateAll {
      Seq(
        validateNameNotBlank(personForm),
        validateNameNotToLong(personForm)
      )
    }.unit

  private def validateNameNotBlank(personForm: PersonForm) =
    ZValidation.fromPredicateWith("Name cannot be blank")(personForm)(personForm => !personForm.name.isBlank)

  private def validateNameNotToLong(personForm: PersonForm) =
    ZValidation.fromPredicateWith("Name cannot be longer then 255 characters")(personForm)(personForm => personForm.name.length <= 255)
}

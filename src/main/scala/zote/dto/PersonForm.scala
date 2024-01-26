package zote.dto

import zio.*
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.prelude.*
import zote.exceptions.ValidationException

case class PersonForm(
    name: String
)

object PersonForm {
    given JsonCodec[PersonForm] = DeriveJsonCodec.gen

    def validateZIO(userForm: PersonForm): IO[ValidationException, Unit] =
        validate(userForm).mapError(ValidationException.apply).toZIO

    private def validate(userForm: PersonForm): Validation[String, Unit] =
        ZValidation.validateAll {
            Seq(
                validateNameNotBlank(userForm),
                validateNameNotToLong(userForm)
            )
        }.unit

    private def validateNameNotBlank(userForm: PersonForm) =
        ZValidation.fromPredicateWith("Title cannot be blank")(userForm)(userForm => !userForm.name.isBlank)

    private def validateNameNotToLong(userForm: PersonForm) =
        ZValidation.fromPredicateWith("Title cannot be longer then 50 characters")(userForm)(userForm => userForm.name.length <= 50)
}

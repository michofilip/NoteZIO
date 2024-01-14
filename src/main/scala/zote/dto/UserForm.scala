package zote.dto

import zio.*
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.prelude.*
import zote.exceptions.ValidationException

case class UserForm(
    name: String
)

object UserForm {
    given JsonCodec[UserForm] = DeriveJsonCodec.gen

    def validateZIO(userForm: UserForm): IO[ValidationException, Unit] =
        validate(userForm).mapError(ValidationException.apply).toZIO

    private def validate(userForm: UserForm): Validation[String, Unit] =
        ZValidation.validateAll {
            Seq(
                validateNameNotBlank(userForm),
                validateNameNotToLong(userForm)
            )
        }.unit

    private def validateNameNotBlank(userForm: UserForm) =
        ZValidation.fromPredicateWith("Title cannot be blank")(userForm)(userForm => !userForm.name.isBlank)

    private def validateNameNotToLong(userForm: UserForm) =
        ZValidation.fromPredicateWith("Title cannot be longer then 50 characters")(userForm)(userForm => userForm.name.length <= 50)
}

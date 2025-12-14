package zote.dto.form

import sttp.tapir.Schema.annotations.*
import zio.*
import zio.json.JsonCodec
import zio.prelude.*
import zote.Validations

case class UserForm(
    name: String,
)

object UserForm {

  @title("UserForm")
  case class Raw(
      name: Option[String] = None,
  ) derives JsonCodec {
    def validate: Validation[String, UserForm] = {
      val validateName = Validations.validateRequired("name", name)(
        Validations.minLength(3),
        Validations.maxLength(255),
      )

      validateName.map(UserForm.apply)
    }
  }
}

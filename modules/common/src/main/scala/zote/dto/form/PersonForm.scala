package zote.dto.form

import sttp.tapir.Schema.annotations.*
import zio.*
import zio.json.JsonCodec
import zio.prelude.*
import zote.Validations

case class PersonForm(
    name: String,
)

object PersonForm {

  @title("PersonForm")
  case class Raw(
      name: Option[String] = None,
  ) derives JsonCodec {
    def validate: Validation[String, PersonForm] = {
      val validateName = Validations.validateRequired("name", name)(
        Validations.minLength(3),
        Validations.maxLength(255),
      )

      validateName.map(PersonForm.apply)
    }
  }
}

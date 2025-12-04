package zote.dto.form

import sttp.tapir.Schema.annotations.*
import zio.*
import zio.json.JsonCodec
import zio.prelude.*
import zote.Validations

case class LabelForm(
    name: String,
)

object LabelForm {

  @title("LabelForm")
  case class Raw(
      name: Option[String] = None,
  ) derives JsonCodec {
    def validate: Validation[String, LabelForm] = {
      val validateName = Validations.validateRequired("name", name)(
        Validations.notBlank,
        Validations.maxLength(50),
      )

      validateName.map(LabelForm.apply)
    }
  }
}

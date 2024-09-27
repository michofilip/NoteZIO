package zote.dto.form

import zio.*
import zio.json.JsonCodec
import zote.dto.validation.Validations
import zote.dto.validation.Validations.*

case class LabelForm(
    name: String
) derives JsonCodec

object LabelForm {

  given Validations[LabelForm] = List(
    notBlank("name", _.name),
    maxLength("name", _.name, 50)
  )
}

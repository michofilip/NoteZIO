package zote.dto.form

import zio.*
import zio.json.JsonCodec
import zote.dto.validation.Validations
import zote.dto.validation.Validations.*

case class PersonForm(
    name: String
) derives JsonCodec

object PersonForm {

  given Validations[PersonForm] = List(
    notBlank("name", _.name),
    minLength("name", _.name, 2),
    maxLength("name", _.name, 255)
  )
}

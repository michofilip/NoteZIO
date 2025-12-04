package zote.dto.form

import sttp.tapir.Schema.annotations.*
import zio.json.JsonCodec
import zio.prelude.Validation
import zote.Ids.PersonId
import zote.Validations
import zote.enums.NotePersonRole

case class NotePersonForm(
    personId: PersonId,
    role: NotePersonRole,
)

object NotePersonForm {

  @title("NotePersonForm")
  case class Raw(
      personId: Option[PersonId] = None,
      role: Option[NotePersonRole] = None,
  ) derives JsonCodec {
    def validate: Validation[String, NotePersonForm] = {
      Validation
        .validateWith(
          Validations.validateRequired("personId", personId)(),
          Validations.validateRequired("role", role)(),
        )(NotePersonForm.apply)
    }
  }
}

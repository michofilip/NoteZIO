package zote.dto.form

import sttp.tapir.Schema.annotations.*
import zio.json.JsonCodec
import zio.prelude.Validation
import zote.Ids.UserId
import zote.Validations
import zote.enums.NoteUserRole

case class NoteUserForm(
    userId: UserId,
    role: NoteUserRole,
)

object NoteUserForm {

  @title("NoteUserForm")
  case class Raw(
      userId: Option[UserId] = None,
      role: Option[NoteUserRole] = None,
  ) derives JsonCodec {
    def validate: Validation[String, NoteUserForm] = {
      Validation
        .validateWith(
          Validations.validateRequired("userId", userId)(),
          Validations.validateRequired("role", role)(),
        )(NoteUserForm.apply)
    }
  }
}

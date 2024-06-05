package zote.dto.form

import zio.json.JsonCodec
import zote.enums.NotePersonRole

case class NotePersonForm(
  personId: Long,
  role: NotePersonRole
)derives JsonCodec

package zote.dto.form

import zio.json.JsonCodec

case class NotePersonForm(
  personId: Long,
  owner: Boolean
)derives JsonCodec

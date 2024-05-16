package zote.dto

import zio.json.JsonCodec

case class NotePersonForm(
  personId: Long,
  owner: Boolean
)derives JsonCodec

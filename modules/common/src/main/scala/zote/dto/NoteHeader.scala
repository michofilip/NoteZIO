package zote.dto

import zio.json.JsonCodec
import zote.enums.NoteStatus

case class NoteHeader(
  id: Long,
  title: String,
  status: NoteStatus,
  labels: Option[List[Label]],
)derives JsonCodec

package zote.dto

import zio.json.JsonCodec
import zote.enums.NoteStatus

case class Note(
  header: NoteHeader,
  message: String,
  assignees: Option[List[NotePerson]],
  parentNote: Option[NoteHeader],
  childrenNotes: Option[List[NoteHeader]],
)derives JsonCodec

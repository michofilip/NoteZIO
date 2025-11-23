package zote.db.model

import io.getquill.*
import zote.Ids.{NoteId, PersonId}
import zote.enums.NotePersonRole

case class NotePersonEntity(
    noteId: NoteId,
    personId: PersonId,
    role: NotePersonRole,
)

object NotePersonEntity {
  inline given SchemaMeta[NotePersonEntity] = schemaMeta(
    "note_person",
    _.noteId   -> "note_id",
    _.personId -> "person_id",
    _.role     -> "role",
  )
}

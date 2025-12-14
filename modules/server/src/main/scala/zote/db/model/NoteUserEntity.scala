package zote.db.model

import io.getquill.*
import zote.Ids.{NoteId, UserId}
import zote.enums.NoteUserRole

case class NoteUserEntity(
    noteId: NoteId,
    userId: UserId,
    role: NoteUserRole,
)

object NoteUserEntity {
  inline given SchemaMeta[NoteUserEntity] = schemaMeta(
    "note_user",
    _.noteId -> "note_id",
    _.userId -> "user_id",
    _.role   -> "role",
  )

  inline given UpdateMeta[NoteUserEntity] = updateMeta[NoteUserEntity](_.noteId, _.userId)
}

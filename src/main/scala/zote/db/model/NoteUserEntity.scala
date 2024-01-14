package zote.db.model

import io.getquill.*

case class NoteUserEntity(
    noteId: Long,
    userId: Long
)

object NoteUserEntity {
    inline given SchemaMeta[NoteUserEntity] = schemaMeta(
        "note_user",
        _.noteId -> "note_id",
        _.userId -> "user_id",
    )
}

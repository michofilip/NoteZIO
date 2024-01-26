package zote.db.model

import io.getquill.*

case class NotePersonEntity(
    noteId: Long,
    personId: Long,
    owner: Boolean
)

object NotePersonEntity {
    inline given SchemaMeta[NotePersonEntity] = schemaMeta(
        "note_person",
        _.noteId -> "note_id",
        _.personId -> "person_id",
        _.owner -> "owner",
    )

    inline given UpdateMeta[NotePersonEntity] = updateMeta[NotePersonEntity](_.noteId, _.personId)
}

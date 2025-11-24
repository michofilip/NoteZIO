package zote.db.model

import io.getquill.*
import zote.Ids.NoteId
import zote.enums.NoteStatus

case class NoteEntity(
    title: String,
    message: Option[String],
    status: NoteStatus,
    parentId: Option[NoteId],
    id: NoteId = NoteId.zero,
)

object NoteEntity {
  inline given SchemaMeta[NoteEntity] = schemaMeta(
    "note",
    _.id       -> "id",
    _.title    -> "title",
    _.message  -> "message",
    _.status   -> "status",
    _.parentId -> "parent_id",
  )

  inline given InsertMeta[NoteEntity] = insertMeta[NoteEntity](_.id)
  inline given UpdateMeta[NoteEntity] = updateMeta[NoteEntity](_.id)
}

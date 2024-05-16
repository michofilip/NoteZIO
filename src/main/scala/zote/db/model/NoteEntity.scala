package zote.db.model

import io.getquill.*
import zote.enums.NoteStatus

case class NoteEntity(
  title: String,
  message: String,
  status: NoteStatus,
  parentId: Option[Long],
  id: Long = 0
)

object NoteEntity {
  inline given SchemaMeta[NoteEntity] = schemaMeta(
    "note",
    _.id -> "id",
    _.title -> "title",
    _.message -> "message",
    _.status -> "status",
    _.parentId -> "parent_id",
  )

  inline given InsertMeta[NoteEntity] = insertMeta[NoteEntity](_.id)

  inline given UpdateMeta[NoteEntity] = updateMeta[NoteEntity](_.id)
}

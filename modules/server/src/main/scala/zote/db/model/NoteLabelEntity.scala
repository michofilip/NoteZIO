package zote.db.model

import io.getquill.*

case class NoteLabelEntity(
    noteId: Long,
    labelId: Long
)

object NoteLabelEntity {
  inline given SchemaMeta[NoteLabelEntity] = schemaMeta(
    "note_label",
    _.noteId -> "note_id",
    _.labelId -> "label_id"
  )
}

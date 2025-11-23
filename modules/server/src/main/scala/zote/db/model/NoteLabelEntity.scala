package zote.db.model

import io.getquill.*
import zote.Ids.{LabelId, NoteId}

case class NoteLabelEntity(
    noteId: NoteId,
    labelId: LabelId,
)

object NoteLabelEntity {
  inline given SchemaMeta[NoteLabelEntity] = schemaMeta(
    "note_label",
    _.noteId  -> "note_id",
    _.labelId -> "label_id",
  )
}

package zote.db.model

import io.getquill.*
import zote.Ids.LabelId

case class LabelEntity(
    name: String,
    id: LabelId = LabelId.zero,
)

object LabelEntity {
  inline given SchemaMeta[LabelEntity] = schemaMeta(
    "label",
    _.id   -> "id",
    _.name -> "name",
  )

  inline given InsertMeta[LabelEntity] = insertMeta[LabelEntity](_.id)
  inline given UpdateMeta[LabelEntity] = updateMeta[LabelEntity](_.id)
}

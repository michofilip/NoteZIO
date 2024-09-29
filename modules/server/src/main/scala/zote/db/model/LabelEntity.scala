package zote.db.model

import io.getquill.*

case class LabelEntity(
    name: String,
    id: Long = 0
)

object LabelEntity {
  inline given SchemaMeta[LabelEntity] = schemaMeta(
    "label",
    _.id -> "id",
    _.name -> "name"
  )

  inline given InsertMeta[LabelEntity] = insertMeta[LabelEntity](_.id)

  inline given UpdateMeta[LabelEntity] = updateMeta[LabelEntity](_.id)
}

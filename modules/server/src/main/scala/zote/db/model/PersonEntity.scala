package zote.db.model

import io.getquill.*

case class PersonEntity(
    name: String,
    id: Long = 0
)

object PersonEntity {
  inline given SchemaMeta[PersonEntity] = schemaMeta(
    "person",
    _.id -> "id",
    _.name -> "name"
  )

  inline given InsertMeta[PersonEntity] = insertMeta[PersonEntity](_.id)

  inline given UpdateMeta[PersonEntity] = updateMeta[PersonEntity](_.id)
}

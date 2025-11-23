package zote.db.model

import io.getquill.*
import zote.Ids.PersonId

case class PersonEntity(
    name: String,
    id: PersonId = PersonId.zero,
)

object PersonEntity {
  inline given SchemaMeta[PersonEntity] = schemaMeta(
    "person",
    _.id   -> "id",
    _.name -> "name",
  )

  inline given InsertMeta[PersonEntity] = insertMeta[PersonEntity](_.id)
  inline given UpdateMeta[PersonEntity] = updateMeta[PersonEntity](_.id)
}

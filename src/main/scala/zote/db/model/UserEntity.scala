package zote.db.model

import io.getquill.*

case class UserEntity(
    name: String,
    id: Long = 0
)

object UserEntity {
    inline given SchemaMeta[UserEntity] = schemaMeta(
        "\"user\"",
        _.id -> "id",
        _.name -> "name",
    )

    inline given InsertMeta[UserEntity] = insertMeta[UserEntity](_.id)

    inline given UpdateMeta[UserEntity] = updateMeta[UserEntity](_.id)
}

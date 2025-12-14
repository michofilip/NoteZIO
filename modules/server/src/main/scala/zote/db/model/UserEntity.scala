package zote.db.model

import io.getquill.*
import zote.Ids.UserId

case class UserEntity(
    name: String,
    id: UserId = UserId.zero,
)

object UserEntity {
  inline given SchemaMeta[UserEntity] = schemaMeta(
    "`user`",
    _.id   -> "id",
    _.name -> "name",
  )

  inline given InsertMeta[UserEntity] = insertMeta[UserEntity](_.id)
  inline given UpdateMeta[UserEntity] = updateMeta[UserEntity](_.id)
}

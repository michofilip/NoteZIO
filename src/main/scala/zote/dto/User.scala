package zote.dto

import zio.json.{DeriveJsonCodec, JsonCodec}

case class User(
    id: Long,
    name: String
)

object User {
    given JsonCodec[User] = DeriveJsonCodec.gen
}

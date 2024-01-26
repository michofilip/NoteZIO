package zote.dto

import zio.json.{DeriveJsonCodec, JsonCodec}

case class Person(
    id: Long,
    name: String
)

object Person {
    given JsonCodec[Person] = DeriveJsonCodec.gen
}

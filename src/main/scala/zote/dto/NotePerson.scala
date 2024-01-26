package zote.dto

import zio.json.{DeriveJsonCodec, JsonCodec}

case class NotePerson(
    person: Person,
    owner: Boolean
)

object NotePerson {
    given JsonCodec[NotePerson] = DeriveJsonCodec.gen
}

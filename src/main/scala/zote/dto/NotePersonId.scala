package zote.dto

import zio.json.{DeriveJsonCodec, JsonCodec}

case class NotePersonId(
    personId: Long,
    owner: Boolean
)

object NotePersonId {
    given JsonCodec[NotePersonId] = DeriveJsonCodec.gen
}

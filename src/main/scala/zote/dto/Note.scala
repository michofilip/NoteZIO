package zote.dto

import zio.json.{DeriveJsonCodec, JsonCodec}
import zote.enums.Status

case class Note(
    id: Long,
    message: String,
    title: String,
    status: Status,
    users: Seq[User]
)

object Note {
    given JsonCodec[Note] = DeriveJsonCodec.gen
}
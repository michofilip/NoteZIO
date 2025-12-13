package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.enums.NotePersonRole

case class NotePerson(
    person: Person,
    role: NotePersonRole,
) derives JsonCodec,
      Schema

package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.enums.NoteUserRole

case class NoteUser(
                     user: User,
                     role: NoteUserRole,
) derives JsonCodec,
      Schema

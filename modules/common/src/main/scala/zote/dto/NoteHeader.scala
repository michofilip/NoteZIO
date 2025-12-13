package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.Ids.NoteId
import zote.enums.NoteStatus

case class NoteHeader(
    id: NoteId,
    title: String,
    status: NoteStatus,
    labels: Option[List[Label]],
) derives JsonCodec,
      Schema

package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.Ids.LabelId

case class Label(
    id: LabelId,
    name: String,
) derives JsonCodec,
      Schema

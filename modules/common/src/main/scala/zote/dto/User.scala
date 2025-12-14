package zote.dto

import sttp.tapir.Schema
import zio.json.JsonCodec
import zote.Ids.UserId

case class User(
    id: UserId,
    name: String,
) derives JsonCodec,
      Schema

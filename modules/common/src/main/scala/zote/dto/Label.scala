package zote.dto

import zio.json.JsonCodec

case class Label(
  id: Long,
  name: String
)derives JsonCodec

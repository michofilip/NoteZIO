package zote.dto

import zio.json.JsonCodec

case class Person(
  id: Long,
  name: String
)derives JsonCodec

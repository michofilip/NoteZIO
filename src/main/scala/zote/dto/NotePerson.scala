package zote.dto

import zio.json.JsonCodec

case class NotePerson(
  person: Person,
  owner: Boolean
)derives JsonCodec

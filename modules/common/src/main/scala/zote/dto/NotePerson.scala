package zote.dto

import zio.json.JsonCodec
import zote.enums.NotePersonRole

case class NotePerson(
  person: Person,
  roles: List[NotePersonRole]
)derives JsonCodec

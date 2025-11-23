package zote.db.repositories

import io.getquill.MappedEncoding
import zote.Ids.{LabelId, NoteId, PersonId}
import zote.enums.*

object includes {
  given MappedEncoding[PersonId, Long] = MappedEncoding(PersonId.value)
  given MappedEncoding[Long, PersonId] = MappedEncoding(PersonId.apply)

  given MappedEncoding[LabelId, Long] = MappedEncoding(LabelId.value)
  given MappedEncoding[Long, LabelId] = MappedEncoding(LabelId.apply)

  given MappedEncoding[NoteId, Long] = MappedEncoding(NoteId.value)
  given MappedEncoding[Long, NoteId] = MappedEncoding(NoteId.apply)

  given MappedEncoding[NotePersonRole, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, NotePersonRole] = MappedEncoding(NotePersonRole.valueOf)

  given MappedEncoding[NoteStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, NoteStatus] = MappedEncoding(NoteStatus.valueOf)
}

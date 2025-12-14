package zote.db.repositories

import io.getquill.MappedEncoding
import zote.Ids.{LabelId, NoteId, UserId}
import zote.enums.*

object includes {
  given MappedEncoding[UserId, Long] = MappedEncoding(UserId.value)
  given MappedEncoding[Long, UserId] = MappedEncoding(UserId.apply)

  given MappedEncoding[LabelId, Long] = MappedEncoding(LabelId.value)
  given MappedEncoding[Long, LabelId] = MappedEncoding(LabelId.apply)

  given MappedEncoding[NoteId, Long] = MappedEncoding(NoteId.value)
  given MappedEncoding[Long, NoteId] = MappedEncoding(NoteId.apply)

  given MappedEncoding[NoteUserRole, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, NoteUserRole] = MappedEncoding(NoteUserRole.valueOf)

  given MappedEncoding[NoteStatus, String] = MappedEncoding(_.toString)
  given MappedEncoding[String, NoteStatus] = MappedEncoding(NoteStatus.valueOf)
}

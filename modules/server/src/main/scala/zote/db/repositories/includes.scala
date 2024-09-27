package zote.db.repositories

import io.getquill.MappedEncoding
import zote.enums.*

object includes {
  given MappedEncoding[NotePersonRole, String] = MappedEncoding(_.toString)

  given MappedEncoding[String, NotePersonRole] = MappedEncoding(
    NotePersonRole.valueOf
  )

  given MappedEncoding[NoteStatus, String] = MappedEncoding(_.toString)

  given MappedEncoding[String, NoteStatus] = MappedEncoding(NoteStatus.valueOf)
}

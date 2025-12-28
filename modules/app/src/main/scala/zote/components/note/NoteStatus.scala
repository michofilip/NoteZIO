package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.NoteHeader

object NoteStatus {
  def apply(noteHeader: Signal[NoteHeader]) = {
    div(
      child <-- noteHeader.map(_.status.toString),
    )
  }
}

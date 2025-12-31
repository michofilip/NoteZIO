package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.NoteHeader

object NoteStatusComponent {
  def apply(noteHeader: Signal[NoteHeader]) = {
    div(
      child <-- noteHeader.map(_.status.toString),
    )
  }
}

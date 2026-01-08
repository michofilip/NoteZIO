package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.{Note, NoteHeader}

object NotesComponent {
  def apply(noteHeaders: Signal[List[NoteHeader]]) = {
    div(
      NoteHeadersComponent(noteHeaders),
    )
  }
}

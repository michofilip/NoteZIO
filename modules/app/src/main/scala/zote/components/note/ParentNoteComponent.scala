package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object ParentNoteComponent {
  def apply(note: Signal[Note]) = {
    div(
      child.maybe <-- note.map(_.parentNote).splitOption { case (_, noteHeader) =>
        NoteTitleComponent.link(noteHeader)
      },
    )
  }
}

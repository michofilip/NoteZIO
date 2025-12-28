package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object ParentNote {
  def apply(note: Signal[Note]) = {
    div(
      child.maybe <-- note.map(_.parentNote).splitOption { case (_, noteHeader) =>
        NoteTitle.link(noteHeader)
      },
    )
  }
}

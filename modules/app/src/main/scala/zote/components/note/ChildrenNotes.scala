package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object ChildrenNotes {
  def apply(note: Signal[Note]) = {
    val childrenNotes = note.map(_.childrenNotes.getOrElse(Nil))

    div(
      ul(
        children <-- childrenNotes.split(_.id) { case (_, _, noteHeader) =>
          li(NoteTitle.link(noteHeader))
        },
      ),
    )
  }
}

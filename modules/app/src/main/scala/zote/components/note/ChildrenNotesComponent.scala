package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object ChildrenNotesComponent {
  def apply(note: Signal[Note]) = {
    val childrenNotes = note.map(_.childrenNotes.getOrElse(Nil))

    div(
      ul(
        children <-- childrenNotes.split(_.id) { case (_, _, noteHeader) =>
          li(NoteTitleComponent.link(noteHeader))
        },
      ),
    )
  }
}

package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.NoteHeader

object NoteLabels {
  def apply(noteHeader: Signal[NoteHeader]) = {
    val labels = noteHeader.map(_.labels.getOrElse(List.empty))

    div(
      ul(
        children <-- labels.split(_.id) { case (_, label, _) =>
          li(label.name)
        },
      ),
    )
  }
}

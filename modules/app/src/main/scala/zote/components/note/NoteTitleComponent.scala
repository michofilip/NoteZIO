package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.NoteHeader
import zote.utils.Paths

object NoteTitleComponent {
  def link(noteHeader: Signal[NoteHeader]) = {
    div(
      a(
        href <-- noteHeader.map(noteHeader => Paths.note(noteHeader.id)),
        child <-- noteHeader.map(_.title),
      ),
    )
  }

  def pretty(noteHeader: Signal[NoteHeader]) = {
    div(
      dataAttr("bs-theme") := "dark",
      h1(child <-- noteHeader.map(_.title)),
    )
  }
}

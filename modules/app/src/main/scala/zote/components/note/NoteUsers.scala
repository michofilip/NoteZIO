package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object NoteUsers {
  def apply(note: Signal[Note]) = {
    val assignees = note.map(_.assignees.getOrElse(Nil))

    div(
      ul(
        children <-- assignees.split(_.user.id) { case (_, noteUser, _) =>
          li(
            div(noteUser.user.name),
            div(noteUser.role.toString),
          )
        },
      ),
    )
  }
}

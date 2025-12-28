package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object NoteMessage {
  def apply(note: Signal[Note]) = {
    div(
      child.maybe <-- note.map(_.message).splitOption { case (message, _) => span(message) },
    )
  }
}

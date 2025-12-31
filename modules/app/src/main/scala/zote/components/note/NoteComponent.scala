package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Note

object NoteComponent {
  def apply(note: Signal[Note]) = {
    val noteHeader = note.map(_.header)

    div(
      NoteTitleComponent.pretty(noteHeader),
      NoteStatusComponent(noteHeader),
      NoteLabelsComponent(noteHeader),
      NoteUsersComponent(note),
      ParentNoteComponent(note),
      ChildrenNotesComponent(note),
      NoteMessageComponent(note),
    )
  }
}

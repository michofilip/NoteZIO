package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.Ids.NoteId
import zote.components.note.NoteComponent
import zote.services.NoteResponseService

object NotePage {
  def apply(noteId: NoteId) = {
    val note = NoteResponseService.getNote

    div(
      onMountCallback(_ => NoteResponseService.fetch(noteId)),
      child.maybe <-- note.splitOption { case (_, note) =>
        NoteComponent(note)
      },
    )
  }
}

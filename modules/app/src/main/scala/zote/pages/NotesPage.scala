package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.components.note.NotesComponent
import zote.services.NotesResponseService

object NotesPage {
  def apply() = {
    val noteHeaders = NotesResponseService.getNoteHeaders

    div(
      onMountCallback(_ => NotesResponseService.fetch()),
      onUnmountCallback(_ => NotesResponseService.clear()),
      child.maybe <-- noteHeaders.splitOption { case (_, noteHeaders) =>
        NotesComponent(noteHeaders)
      },
    )
  }
}

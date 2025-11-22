package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.components.NoteHeadersTable
import zote.dto.NoteHeader
import zote.utils.BackendClient

object NoteListPage {
  def apply() = {
    val noteHeaders = Var(List.empty[NoteHeader])

    div(
      onMountCallback(_ => BackendClient.notes.getAll(noteHeaders.set)),
      h1("Note list"),
      NoteHeadersTable(noteHeaders.signal)
    )
  }
}

package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.Ids.NoteId
import zote.pages.{NotFoundPage, NotePage, NotesPage}

object NotesRoutes {
  def apply() = {
    div(
      pathEnd {
        NotesPage()
      },
      path(long) { noteId =>
        NotePage(NoteId(noteId))
      },
      noneMatched {
        NotFoundPage()
      },
    )
  }
}

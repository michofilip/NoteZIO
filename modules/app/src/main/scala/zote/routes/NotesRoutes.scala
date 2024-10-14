package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.pages.{NotFoundPage, NoteListPage, NotePage}

object NotesRoutes {
  def apply() = {
    div(
      pathEnd {
        NoteListPage()
      },
      path(long) { noteId =>
        NotePage(noteId)
      },
      noneMatched {
        NotFoundPage()
      }
    )
  }
}

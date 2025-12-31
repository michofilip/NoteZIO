package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.pages.*
import zote.utils.Paths

object Routes {
  def apply() = {
    routes(
      div(
        pathEnd {
          navigate(Paths.notesPrefix, replace = true)
        },
        pathPrefix(Paths.notesPrefix) {
          NotesRoutes()
        },
        pathPrefix(Paths.usersPrefix) {
          UsersRoutes()
        },
        pathPrefix(Paths.labelsPrefix) {
          LabelsRoutes()
        },
        noneMatched {
          NotFoundPage()
        },
      ),
    )
  }
}

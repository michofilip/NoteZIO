package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.pages.*

object Routes {
  def apply() = {
    routes(
      div(
        pathEnd {
          navigate("notes", replace = true)
        },
        pathPrefix("notes") {
          NotesRoutes()
        },
        pathPrefix("persons") {
          PersonsRoutes()
        },
        pathPrefix("labels") {
          LabelsRoutes()
        },
        noneMatched {
          NotFoundPage()
        },
      ),
    )
  }
}

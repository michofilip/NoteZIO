package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.pages.{NotFoundPage, PersonListPage, PersonPage}

object PersonsRoutes {
  def apply() = {
    div(
      pathEnd {
        PersonListPage()
      },
      path(long) { personId =>
        PersonPage(personId)
      },
      noneMatched {
        NotFoundPage()
      }
    )
  }
}

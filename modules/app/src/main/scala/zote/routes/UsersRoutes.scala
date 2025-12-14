package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.pages.{NotFoundPage, UserListPage, UserPage}

object UsersRoutes {
  def apply() = {
    div(
      pathEnd {
        UserListPage()
      },
      path(long) { userId =>
        UserPage(userId)
      },
      noneMatched {
        NotFoundPage()
      },
    )
  }
}

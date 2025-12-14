package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.dto.User
import zote.utils.BackendClient

object UserPage {
  def apply(userId: Long) = {
    val user = Var[Option[User]](None)

    div(
//      onMountCallback(_ => BackendClient.users.getById(userId)(p => user.set(Some(p)))),
      div(
        child <-- user.signal.map(_.map { user =>
          div(
            div("Name"),
            div(user.name),
          )
        }.getOrElse(emptyNode)),
      ),
    )
  }
}

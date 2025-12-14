package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.components.UsersTable
import zote.dto.User
import zote.utils.BackendClient

object UserListPage {
  def apply() = {
    val users = Var(List.empty[User])

    div(
//      onMountCallback(_ => BackendClient.users.getAll(users.set)),
      h1("User list"),
      UsersTable(users.signal),
    )
  }
}

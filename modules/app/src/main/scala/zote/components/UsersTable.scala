package zote.components

import com.raquo.laminar.api.L.{*, given}
import zote.Ids.UserId
import zote.dto.User

object UsersTable {

  def apply(users: Signal[List[User]]) = {
    div(renderTable(users))
  }

  def renderTable(users: Signal[List[User]]) = {
    table(
      renderTableHeader(),
      renderTableBody(users),
      renderTableFooter(),
    )
  }

  private def renderTableHeader() = {
    thead(
      tr(
        th("Name"),
      ),
    )
  }

  private def renderTableBody(users: Signal[List[User]]) = {
    tbody(
      children <-- users.split(_.id) { case (id, _, user) =>
        renderRow(id, user)
      },
    )
  }

  private def renderTableFooter() = {
    tfoot(
      th("Name"),
    )
  }

  private def renderRow(
      id: UserId,
      user: Signal[User],
  ) = {
    tr(
      td(
        a(
          href <-- user.map(user => s"/users/${user.id.value}"),
          child <-- user.map(_.name),
        ),
      ),
    )
  }
}

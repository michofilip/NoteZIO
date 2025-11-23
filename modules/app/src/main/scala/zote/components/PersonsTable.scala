package zote.components

import com.raquo.laminar.api.L.{*, given}
import zote.Ids.PersonId
import zote.dto.Person

object PersonsTable {

  def apply(persons: Signal[List[Person]]) = {
    div(renderTable(persons))
  }

  def renderTable(persons: Signal[List[Person]]) = {
    table(
      renderTableHeader(),
      renderTableBody(persons),
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

  private def renderTableBody(persons: Signal[List[Person]]) = {
    tbody(
      children <-- persons.split(_.id) { case (id, _, person) =>
        renderRow(id, person)
      },
    )
  }

  private def renderTableFooter() = {
    tfoot(
      th("Name"),
    )
  }

  private def renderRow(
      id: PersonId,
      person: Signal[Person],
  ) = {
    tr(
      td(
        a(
          href <-- person.map(person => s"/persons/${person.id.value}"),
          child <-- person.map(_.name),
        ),
      ),
    )
  }
}

package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.components.PersonsTable
import zote.dto.Person
import zote.utils.BackendClient

object PersonListPage {
  def apply() = {
    val persons = Var(List.empty[Person])

    div(
//      onMountCallback(_ => BackendClient.persons.getAll(persons.set)),
      h1("Person list"),
      PersonsTable(persons.signal),
    )
  }
}

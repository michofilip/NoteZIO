package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.dto.Person
import zote.utils.BackendClient

object PersonPage {
  def apply(personId: Long) = {
    val person = Var[Option[Person]](None)

    div(
//      onMountCallback(_ => BackendClient.persons.getById(personId)(p => person.set(Some(p)))),
      div(
        child <-- person.signal.map(_.map { person =>
          div(
            div("Name"),
            div(person.name),
          )
        }.getOrElse(emptyNode)),
      ),
    )
  }
}

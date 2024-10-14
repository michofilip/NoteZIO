package zote.pages

import com.raquo.laminar.api.L.{*, given}

object PersonPage {
  def apply(personId: Long) = {
    div(
      s"Person $personId"
    )
  }
}

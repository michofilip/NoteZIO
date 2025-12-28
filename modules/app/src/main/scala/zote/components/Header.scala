package zote.components

import com.raquo.laminar.api.L.{*, given}
import zote.utils.Paths

object Header {
  def apply() = {
    div(
      ul(
        li(div(a(href := Paths.notes, "Notes"))),
        li(div(a(href := Paths.users, "Users"))),
        li(div(a(href := Paths.labels, "Labels"))),
      ),
    )
  }
}

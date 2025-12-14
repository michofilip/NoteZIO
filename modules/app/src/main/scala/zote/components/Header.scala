package zote.components

import com.raquo.laminar.api.L.{*, given}

object Header {
  def apply() = {
    div(
      div(a(href := "/notes", "Notes")),
      div(a(href := "/users", "Users")),
      div(a(href := "/labels", "Labels")),
    )
  }
}

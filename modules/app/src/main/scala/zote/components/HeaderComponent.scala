package zote.components

import com.raquo.laminar.api.L.{*, given}
import zote.utils.Paths

object HeaderComponent {
  def apply() = {
    navTag(
      cls := List("navbar", "navbar-expand-lg", "bg-body-tertiary"),
      div(
        cls := "container-fluid",
        a(cls := "navbar-brand", href := "/", "Zote"),
        button(
          cls                   := "navbar-toggler",
          tpe                   := "button",
          dataAttr("bs-toggle") := "collapse",
          dataAttr("bs-target") := "#navbarNav",
          aria.controls         := "navbarNav",
          aria.expanded         := false,
          aria.label            := "Toggle navigation",
          span(cls := "navbar-toggler-icon"),
        ),
        navItems(),
      ),
    )
  }

  private def navItems() = {
    div(
      cls    := List("collapse", "navbar-collapse"),
      idAttr := "navbarNav",
      ul(
        cls := "navbar-nav",
        navItem(Paths.notes, description = "Notes"),
        navItem(Paths.users, description = "Users"),
        navItem(Paths.labels, description = "Labels"),
      ),
    )
  }

  private def navItem(path: String, description: String) = {
    val isActive = Paths.contains(path)
    val classes  = isActive.map(active => List("nav-link") ++ Option.when(active)("active"))
    val current  = isActive.map(active => if (active) "page" else "")

    li(
      cls := "nav-item",
      a(
        cls <-- classes,
        aria.current <-- current,
        href := path,
        description,
      ),
    )
  }
}

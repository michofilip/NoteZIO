package zote

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import org.scalajs.dom
import zote.components.HeaderComponent
import zote.routes.Routes

object App {

  def main(args: Array[String]): Unit = {
    renderOnDomContentLoaded(
      dom.document.getElementById("app"),
      app().amend(LinkHandler.bind),
    )
  }

  private def app() = {
    div(
      HeaderComponent(),
      Routes(),
    )
  }
}

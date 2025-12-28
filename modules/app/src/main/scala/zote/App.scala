package zote

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import org.scalajs.dom
import zote.components.Header
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
      Header(),
      Routes(),
    )
  }
}

package zote

import com.raquo.laminar.api.L.{*, given}
import frontroute.LinkHandler
import org.scalajs.dom

object App {

  def main(args: Array[String]): Unit = {
    renderOnDomContentLoaded(
      dom.document.getElementById("app"),
      appElement().amend(LinkHandler.bind)
    )
  }

  def appElement(): Element = {
    val clicks = Var(0)
    div(
      div(
        "Button was clicked: ",
        child <-- clicks.signal.map(_.toString),
        " times"
      ),
      div(
        button(
          tpe := "button",
          onClick --> (_ => clicks.update(_ + 1)),
          "+"
        ),
        button(
          tpe := "button",
          onClick --> (_ => clicks.update(_ - 1)),
          "-"
        )
      )
    )
  }

}

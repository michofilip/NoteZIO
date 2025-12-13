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
//      Demo3.appElement().amend(LinkHandler.bind)
      app()
        .amend(LinkHandler.bind),
    )
  }

  private def app() = {
    div(
      Header(),
      Routes(),
    )
  }

  def appElement(): Element = {
    val clicks = Var(0)
    div(
      div(
        "Button was clicked: ",
        child <-- clicks.signal.map(_.toString),
        " times",
      ),
      div(
        button(
          tpe := "button",
          onClick --> (_ => clicks.update(_ + 1)),
          "+",
        ),
        button(
          tpe := "button",
          onClick --> (_ => clicks.update(_ - 1)),
          "-",
        ),
      ),
    )
  }

  object Demo2 {
    import scala.util.Random

    final case class DataItemID(value: Long)

    case class DataItem(
        id: DataItemID,
        label: String,
        price: Double,
        count: Int,
    ) {
      def fullPrice: Double = price * count
    }

    object DataItem {

      def apply(dataItemID: DataItemID): DataItem =
        DataItem(
          dataItemID,
          s"Item ${dataItemID.value}",
          Random.between(10, 100000) / 100.0,
          Random.nextInt(5) + 1,
        )
    }

    type DataList = List[DataItem]

    object Model {
      val idVar = Var(1L)

      val dataVar: Var[DataList] = Var(
        List.empty,
      )
      val dataSignal = dataVar.signal

      def addDataItem(item: DataItem): Unit =
        dataVar.update(data => data :+ item)

      def removeDataItem(id: DataItemID): Unit =
        dataVar.update(data => data.filter(_.id != id))
    }

    def appElement(): Element = {
      div(div("Data Items"), div(renderTable()))
    }

    def renderTable() = {
      table(
        renderTableHeader(),
        renderTableBody(),
        renderTableFooter(),
      )
    }

    private def renderTableHeader() = {
      thead(
        tr(
          th("ID"),
          th("Label"),
          th("Price"),
          th("Count"),
          th("Total price"),
          th("Remove"),
        ),
      )
    }

    private def renderTableBody() = {
      tbody(
        children <-- Model.dataSignal.split(_.id) { case (id, _, signal) =>
          renderRow(id, signal)
        },
      )
    }

    private def renderTableFooter() = {
      tfoot(
        th(
          button(
            tpe := "button",
            onClick --> (_ => {
              Model.addDataItem(
                DataItem(DataItemID(Model.idVar.signal.now())),
              )
              Model.idVar.update(_ + 1)
            }),
            "Add item",
          ),
        ),
        th(),
        th(),
        th(),
        th(
          child <-- Model.dataSignal
            .map(_.map(_.fullPrice).sum)
            .map(sum => f"$sum%.2f zł"),
        ),
        th(),
      )

    }

    private def renderRow(
        dataItemID: DataItemID,
        dataItemSignal: Signal[DataItem],
    ) = {
      tr(
        td(dataItemID.value),
        td(child <-- dataItemSignal.map(_.label)),
        td(
          child <-- dataItemSignal.map(dataItem => f"${dataItem.price}%.2f zł"),
        ),
        td((child <-- dataItemSignal.map(_.count))),
        td(
          child <-- dataItemSignal.map(dataItem => f"${dataItem.fullPrice}%.2f zł"),
        ),
        td(
          button(
            tpe := "button",
            onClick --> (_ => Model.removeDataItem(dataItemID)),
            "Remove",
          ),
        ),
      )

    }

  }

  object Demo3 {
    def appElement() = {
      routes(
        div(
          firstMatch(
            path("blog") {
              div("Blog")
            },
            path("news") {
              div("News")
            },
            div("Not found"),
          ),
        ),
//        div(
//          path("blog") {
//            div("Blog")
//          },
//          path("news") {
//            div("News")
//          },
//          noneMatched {
//            div("Not found")
//          }
//        )
      )
    }
  }

}

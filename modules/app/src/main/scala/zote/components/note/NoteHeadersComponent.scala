package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import com.raquo.laminar.codecs.StringAsIsCodec
import zote.Ids.NoteId
import zote.dto.NoteHeader

object NoteHeadersComponent {

  def apply(noteHeaders: Signal[List[NoteHeader]]) = {
    div(
      table(
        cls := List("table", "caption-top"),
        caption("Notes"),
        renderTableHeader(),
        renderTableBody(noteHeaders),
        renderTableFooter(),
      ),
    )
  }

  private def renderTableHeader() = {
    thead(
      tr(
        th(htmlAttr("scope", StringAsIsCodec) := "col", "Title"),
        th(htmlAttr("scope", StringAsIsCodec) := "col", "Status"),
        th(htmlAttr("scope", StringAsIsCodec) := "col", "Labels"),
      ),
    )
  }

  private def renderTableBody(noteHeaders: Signal[List[NoteHeader]]) = {
    tbody(
      children <-- noteHeaders.split(_.id) { case (_, _, noteHeader) => renderRow(noteHeader) },
    )
  }

  private def renderRow(noteHeader: Signal[NoteHeader]) = {
    tr(
      th(htmlAttr("scope", StringAsIsCodec) := "row", NoteTitleComponent.link(noteHeader)),
      td(NoteStatusComponent(noteHeader)),
      td(NoteLabelsComponent(noteHeader)),
    )
  }

  private def renderTableFooter() = {
    tfoot(
      th(htmlAttr("scope", StringAsIsCodec) := "col", "Title"),
      th(htmlAttr("scope", StringAsIsCodec) := "col", "Status"),
      th(htmlAttr("scope", StringAsIsCodec) := "col", "Labels"),
    )
  }
}

package zote.components.note

import com.raquo.laminar.api.L.{*, given}
import zote.Ids.NoteId
import zote.dto.NoteHeader

object NoteHeadersTable {

  def apply(noteHeaders: Signal[List[NoteHeader]]) = {
    div(
      table(
        renderTableHeader(),
        renderTableBody(noteHeaders),
        renderTableFooter(),
      ),
    )
  }

  private def renderTableHeader() = {
    thead(
      tr(
        th("Title"),
        th("Status"),
        th("Labels"),
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
      td(NoteTitle.link(noteHeader)),
      td(NoteStatus(noteHeader)),
      td(NoteLabels(noteHeader)),
    )
  }

  private def renderTableFooter() = {
    tfoot(
      th("Title"),
      th("Status"),
      th("Labels"),
    )
  }
}

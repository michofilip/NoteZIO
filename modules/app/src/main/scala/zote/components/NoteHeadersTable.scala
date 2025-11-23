package zote.components

import com.raquo.laminar.api.L.{*, given}
import zote.Ids.NoteId
import zote.dto.NoteHeader

object NoteHeadersTable {

  def apply(noteHeaders: Signal[List[NoteHeader]]) = {
    div(renderTable(noteHeaders))
  }

  def renderTable(noteHeaders: Signal[List[NoteHeader]]) = {
    table(
      renderTableHeader(),
      renderTableBody(noteHeaders),
      renderTableFooter(),
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
      children <-- noteHeaders.split(_.id) { case (id, _, noteHeader) =>
        renderRow(id, noteHeader)
      },
    )
  }

  private def renderTableFooter() = {
    tfoot(
      th("Title"),
      th("Status"),
      th("Labels"),
    )
  }

  private def renderRow(
      id: NoteId,
      noteHeader: Signal[NoteHeader],
  ) = {
    val labels = noteHeader.map(_.labels.getOrElse(List.empty))

    tr(
      td(
        a(
          href <-- noteHeader.map(noteHeader => s"/notes/${noteHeader.id}"),
          child <-- noteHeader.map(_.title),
        ),
      ),
      td(child <-- noteHeader.map(_.status.toString)),
      td(
        Labels(labels),
      ),
    )
  }
}

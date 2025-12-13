package zote.pages

import com.raquo.laminar.api.L.{*, given}
import zote.components.Labels
import zote.dto.Note
import zote.utils.BackendClient

object NotePage {
  def apply(noteId: Long) = {
    val note   = Var(Option.empty[Note])
    val labels =
      note.signal.map(_.flatMap(_.header.labels).getOrElse(List.empty))

    div(
//      onMountCallback(_ => BackendClient.notes.getById(noteId)(n => note.set(Some(n)))),
      s"Note $noteId",
      div(
        child <-- note.signal.map(_.map { note =>
          div(
            div(note.header.title),
            Labels(labels),
            div(note.message),
          )
        }.getOrElse(emptyNode)),
      ),
    )
  }
}

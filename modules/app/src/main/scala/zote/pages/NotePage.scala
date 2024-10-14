package zote.pages

import com.raquo.laminar.api.L.{*, given}

object NotePage {
  def apply(noteId: Long) = {
    div(
      s"Note $noteId"
    )
  }
}

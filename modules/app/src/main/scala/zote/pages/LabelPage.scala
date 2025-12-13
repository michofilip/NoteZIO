package zote.pages

import com.raquo.laminar.api.L.{*, given}

object LabelPage {
  def apply(labelId: Long) = {
    div(
      s"Label $labelId",
    )
  }
}

package zote.components

import com.raquo.laminar.api.L.{*, given}
import zote.dto

object Labels {
  def apply(labels: Signal[List[dto.Label]]) = {
    div(
      children <-- labels.map(labels => labels.map(label => span(label.name))),
    )
  }
}

package zote.routes

import com.raquo.laminar.api.L.{*, given}
import frontroute.*
import zote.pages.*

object LabelsRoutes {
  def apply() = {
    div(
      pathEnd {
        LabelListPage()
      },
      path(long) { labelId =>
        LabelPage(labelId)
      },
      noneMatched {
        NotFoundPage()
      }
    )
  }
}

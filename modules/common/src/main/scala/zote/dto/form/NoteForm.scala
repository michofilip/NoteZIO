package zote.dto.form

import zio.*
import zio.json.JsonCodec
import zote.Ids.{LabelId, NoteId}
import zote.dto.validation.Validations.*
import zote.enums.NoteStatus

case class NoteForm(
    title: String,
    message: String,
    status: NoteStatus,
    assignees: Set[NotePersonForm],
    parentId: Option[NoteId],
    labels: Set[LabelId],
) derives JsonCodec

object NoteForm {

  given Validations[NoteForm] = List(
    notBlank("title", _.title),
    maxLength("title", _.title, 50),
    notBlank("message", _.message),
  )
}

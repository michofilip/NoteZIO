package zote.services.validation

import zio.*
import zote.dto.form.NoteForm
import zote.exceptions.ValidationException

trait NoteValidationService {
  def validate(noteForm: NoteForm.Raw): Task[NoteForm]
}

case class NoteValidationServiceImpl() extends NoteValidationService {
  override def validate(noteForm: NoteForm.Raw): Task[NoteForm] = {
    noteForm.validate
      .mapError(ValidationException.apply)
      .toZIOAssociative
  }
}

object NoteValidationServiceImpl {
  lazy val layer = ZLayer.derive[NoteValidationServiceImpl]
}

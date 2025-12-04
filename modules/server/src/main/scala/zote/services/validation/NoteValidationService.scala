package zote.services.validation

import zio.*
import zote.db.repositories.*
import zote.dto.form.NoteForm
import zote.exceptions.ValidationException

trait NoteValidationService {
  def validate(noteForm: NoteForm.Raw): Task[NoteForm]
}

case class NoteValidationServiceImpl(
    private val labelRepository: LabelRepository,
    private val noteRepository: NoteRepository,
    private val personRepository: PersonRepository,
) extends NoteValidationService {
  override def validate(noteForm: NoteForm.Raw): Task[NoteForm] = {
    for {
      noteForm <- noteForm.validate
        .mapError(ValidationException.apply)
        .toZIOAssociative
      _ <- ZIO.foreachDiscard(noteForm.parentId)(noteRepository.getById) <&>
        ZIO.foreachParDiscard(noteForm.assignees.map(_.personId))(personRepository.getById) <&>
        ZIO.foreachParDiscard(noteForm.labels)(labelRepository.getById)
    } yield noteForm
  }
}

object NoteValidationServiceImpl {
  lazy val layer = ZLayer.derive[NoteValidationServiceImpl]
}

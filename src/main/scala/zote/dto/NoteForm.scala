package zote.dto

import zio.*
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.prelude.*
import zote.enums.NoteStatus
import zote.exceptions.ValidationException

case class NoteForm(
  title: String,
  message: String,
  status: NoteStatus,
  assignees: Set[NotePersonForm],
  parentId: Option[Long],
  labels: Set[Long],
)derives JsonCodec

object NoteForm {
  def validateZIO(noteForm: NoteForm): IO[ValidationException, Unit] =
    validate(noteForm).mapError(ValidationException.apply).toZIO

  private def validate(noteForm: NoteForm): Validation[String, Unit] =
    ZValidation.validateAll {
      Seq(
        validateTitleNotBlank(noteForm),
        validateTitleNotToLong(noteForm),
        validateMessageNotBlank(noteForm),
        validateOwners(noteForm)
      )
    }.unit

  private def validateTitleNotBlank(noteForm: NoteForm) =
    ZValidation.fromPredicateWith("Title cannot be blank")(noteForm)(noteForm => !noteForm.title.isBlank)

  private def validateTitleNotToLong(noteForm: NoteForm) =
    ZValidation.fromPredicateWith("Title cannot be longer then 255 characters")(noteForm)(noteForm => noteForm.title.length <= 50)

  private def validateMessageNotBlank(noteForm: NoteForm) =
    ZValidation.fromPredicateWith("Message cannot be blank")(noteForm)(noteForm => !noteForm.message.isBlank)

  private def validateOwners(noteForm: NoteForm) =
    ZValidation.fromPredicateWith("Same person cannot be both owner and not owner")(noteForm) { noteForm =>
      !noteForm.assignees.groupMap(_.personId)(_.owner).values.exists(_.size > 1)
    }
}

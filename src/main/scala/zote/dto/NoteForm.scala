package zote.dto

import zio.*
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.prelude.*
import zote.enums.Status
import zote.exceptions.ValidationException

case class NoteForm(
    title: String,
    message: String,
    status: Status,
    userIds: Set[Long]
)

object NoteForm {
    given JsonCodec[NoteForm] = DeriveJsonCodec.gen

    def validateZIO(noteForm: NoteForm): IO[ValidationException, Unit] =
        validate(noteForm).mapError(ValidationException.apply).toZIO

    private def validate(noteForm: NoteForm): Validation[String, Unit] =
        ZValidation.validateAll {
            Seq(
                validateTitleNotBlank(noteForm),
                validateTitleNotToLong(noteForm),
                validateMessageNotBlank(noteForm)
            )
        }.unit

    private def validateTitleNotBlank(noteForm: NoteForm) =
        ZValidation.fromPredicateWith("Title cannot be blank")(noteForm)(noteForm => !noteForm.title.isBlank)

    private def validateTitleNotToLong(noteForm: NoteForm) =
        ZValidation.fromPredicateWith("Title cannot be longer then 50 characters")(noteForm)(noteForm => noteForm.title.length <= 50)

    private def validateMessageNotBlank(noteForm: NoteForm) =
        ZValidation.fromPredicateWith("Message cannot be blank")(noteForm)(noteForm => !noteForm.message.isBlank)
}

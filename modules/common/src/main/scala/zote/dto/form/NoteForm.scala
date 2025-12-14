package zote.dto.form

import sttp.tapir.Schema.annotations.*
import zio.*
import zio.json.JsonCodec
import zio.prelude.*
import zote.Ids.{LabelId, NoteId}
import zote.Validations
import zote.enums.NoteStatus

case class NoteForm(
    title: String,
    status: NoteStatus,
    message: Option[String],
    assignees: Set[NoteUserForm],
    parentId: Option[NoteId],
    labels: Set[LabelId],
)

object NoteForm {

  @title("NoteForm")
  case class Raw(
      title: Option[String] = None,
      status: Option[NoteStatus] = None,
      message: Option[String] = None,
      assignees: Option[Set[NoteUserForm.Raw]] = None,
      parentId: Option[NoteId] = None,
      labels: Option[Set[LabelId]] = None,
  ) derives JsonCodec {
    def validate: Validation[String, NoteForm] = {
      val validateTitle = Validations.validateRequired("title", title)(
        Validations.notBlank,
        Validations.maxLength(255),
      )
      val validateStatus    = Validations.validateRequired("status", status)()
      val validateMessage   = Validations.validateOptional("message", message)()
      val validateAssignees = Validations
        .validateOptional("assignees", assignees)()
        .flatMap(validateNoteUserForms)
        .map(_.getOrElse(Set.empty))
      val validateParentId = Validations.validateOptional("parentId", parentId)()
      val validateLabels   = Validations.validateOptional("labels", labels)().map(_.getOrElse(Set.empty))

      Validation
        .validateWith(
          validateTitle,
          validateStatus,
          validateMessage,
          validateAssignees,
          validateParentId,
          validateLabels,
        )(NoteForm.apply)
    }

    private def validateNoteUserForms(
        maybeNoteUserForms: Option[Set[NoteUserForm.Raw]],
    ): Validation[String, Option[Set[NoteUserForm]]] = {
      Validation.validateAll {
        maybeNoteUserForms.map { noteUserForms =>
          Validation.validateAll(noteUserForms.map(_.validate))
        }
      }
    }
  }
}

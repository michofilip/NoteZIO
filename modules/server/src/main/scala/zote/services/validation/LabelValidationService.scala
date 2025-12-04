package zote.services.validation

import zio.*
import zote.dto.form.LabelForm
import zote.exceptions.ValidationException

trait LabelValidationService {
  def validate(labelForm: LabelForm.Raw): Task[LabelForm]
}

case class LabelValidationServiceImpl() extends LabelValidationService {
  override def validate(labelForm: LabelForm.Raw): Task[LabelForm] = {
    labelForm.validate
      .mapError(ValidationException.apply)
      .toZIOAssociative
  }
}

object LabelValidationServiceImpl {
  lazy val layer = ZLayer.derive[LabelValidationServiceImpl]
}

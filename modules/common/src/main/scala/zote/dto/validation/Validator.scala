package zote.dto.validation

import zio.*
import zio.prelude.{Associative, ZValidation}
import zote.dto.validation.Validations.Validations
import zote.exceptions.ValidationException

object Validator {
  private given Associative[ValidationException] =
    new Associative[ValidationException] {
      override def combine(
          l: => ValidationException,
          r: => ValidationException
      ): ValidationException = ValidationException(l.messages ++ r.messages)
    }

  def validateZIO[T: Validations](entity: T): IO[ValidationException, Unit] =
    ZValidation
      .validateAll(summon[Validations[T]].map(_(entity)))
      .mapError(ValidationException.apply)
      .unit
      .toZIOAssociative
}

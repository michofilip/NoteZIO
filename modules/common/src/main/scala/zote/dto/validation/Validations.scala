package zote.dto.validation

import zio.prelude.{Validation, ZValidation}

object Validations {
  type ValidFun[T] = T => Validation[String, T]
  type Validations[T] = Seq[ValidFun[T]]

  def notBlank[T](
      label: String,
      extractor: T => String
  ): ValidFun[T] = { value =>
    ZValidation.fromPredicateWith(s"${label.capitalize} cannot be blank")(
      value
    ) { value =>
      !extractor(value).isBlank
    }
  }

  def minLength[T](
      label: String,
      extractor: T => String,
      minLength: Int
  ): ValidFun[T] = { value =>
    ZValidation.fromPredicateWith(
      s"${label.capitalize} cannot be shorter then $minLength characters"
    )(
      value
    ) { value =>
      extractor(value).length >= minLength
    }
  }

  def maxLength[T](
      label: String,
      extractor: T => String,
      maxLength: Int
  ): ValidFun[T] = { value =>
    ZValidation.fromPredicateWith(
      s"${label.capitalize} cannot be longer then $maxLength characters"
    )(
      value
    ) { value =>
      extractor(value).length <= maxLength
    }
  }
}

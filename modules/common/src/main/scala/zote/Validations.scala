package zote

import zio.*
import zio.prelude.*
import zote.exceptions.ValidationException

object Validations {
  type VIO[+A] = ZIO[Any, ValidationException, A]

  def validateRequired[T](label: String, value: Option[T])(
      validations: ((String, Option[T]) => Validation[String, Option[T]])*,
  ): Validation[String, T] = {
    Validation
      .validate(
        Validation.fromOptionWith(s"$label is required")(value),
        Validation.validateAll(validations.map(_(label, value))),
      )
      .map(_._1)
  }

  def validateOptional[T](label: String, value: Option[T])(
      validations: ((String, Option[T]) => Validation[String, Option[T]])*,
  ): Validation[String, Option[T]] = {
    Validation
      .validate(
        Validation.succeed(value),
        Validation.validateAll(validations.map(_(label, value))),
      )
      .map(_._1)
  }

  def min(min: Int, inclusive: Boolean = true)(
      label: String,
      value: Option[Int],
  ): Validation[String, Option[Int]] = {
    def isValid(value: Int) = if (inclusive) value >= min else value > min

    def error =
      if (inclusive) s"$label must be more or equal to $min"
      else s"$label must be more then $min"

    validateIfPresent(value, isValid, error)
  }

  def max(max: Int, inclusive: Boolean = true)(
      label: String,
      value: Option[Int],
  ): Validation[String, Option[Int]] = {
    def isValid(value: Int) = if (inclusive) value <= max else value < max

    def error =
      if (inclusive) s"$label must be less or equal to $max"
      else s"$label must be less then $max"

    validateIfPresent(value, isValid, error)
  }

  def notEmpty(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = value.nonEmpty
    def error                  = s"$label must not be empty"

    validateIfPresent(value, isValid, error)
  }

  def notBlank(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = !value.isBlank
    def error                  = s"$label must not be blank"

    validateIfPresent(value, isValid, error)
  }

  def minLength(minLength: Int, inclusive: Boolean = true)(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = if (inclusive) value.length >= minLength else value.length > minLength
    def error =
      if (inclusive) s"$label must be longer or equal to $minLength"
      else s"$label must be longer then $minLength"

    validateIfPresent(value, isValid, error)
  }

  def maxLength(maxLength: Int, inclusive: Boolean = true)(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = if (inclusive) value.length <= maxLength else value.length < maxLength
    def error =
      if (inclusive) s"$label must be shorter or equal to $maxLength"
      else s"$label must be shorter then $maxLength"

    validateIfPresent(value, isValid, error)
  }

  def containsLowercase(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = value.toList.exists(_.isLower)
    def error                  = s"$label must contain lower case letter"

    validateIfPresent(value, isValid, error)
  }

  def containsUppercase(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = value.toList.exists(_.isUpper)
    def error                  = s"$label must contain upper case letter"

    validateIfPresent(value, isValid, error)
  }

  def containsDigit(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = value.toList.exists(_.isDigit)
    def error                  = s"$label must contain digit"

    validateIfPresent(value, isValid, error)
  }

  def containsAny(character: Char, characters: Char*)(
      label: String,
      value: Option[String],
  ): Validation[String, Option[String]] = {
    def isValid(value: String) = value.toList.contains(character) || value.toList.exists(characters.contains)
    def error =
      if (characters.isEmpty) s"$label must contain $character"
      else s"$label must contain any of the characters: ${(character :: characters.toList).mkString(", ")}"

    validateIfPresent(value, isValid, error)
  }

  private def validateIfPresent[T](
      value: Option[T],
      isValid: T => Boolean,
      error: => String,
  ): Validation[String, Option[T]] = {
    value match {
      case Some(value) if !isValid(value) => Validation.fail(error)
      case _                              => Validation.succeed(value)
    }
  }
}

package zote.exceptions

case class ValidationException(messages: List[String])
    extends RuntimeException {
  override def getMessage: String = messages.mkString(";")
}

object ValidationException {
  def apply(message: String): ValidationException =
    ValidationException(List(message))
}

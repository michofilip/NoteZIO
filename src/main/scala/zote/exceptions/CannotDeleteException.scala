package zote.exceptions

case class CannotDeleteException(private val message: String) extends RuntimeException {
  override def getMessage: String = message
}

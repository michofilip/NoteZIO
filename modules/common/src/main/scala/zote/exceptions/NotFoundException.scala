package zote.exceptions

case class NotFoundException(message: String) extends RuntimeException {
  override def getMessage: String = message
}

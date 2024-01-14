package zote.exceptions

case class ValidationException(private val message: String) extends RuntimeException {
    override def getMessage: String = message
}

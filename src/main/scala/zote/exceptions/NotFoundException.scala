package zote.exceptions

case class NotFoundException(private val message: String) extends RuntimeException {
    override def getMessage: String = message
}

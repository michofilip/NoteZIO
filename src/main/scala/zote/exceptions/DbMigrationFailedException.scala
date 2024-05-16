package zote.exceptions

case class DbMigrationFailedException(private val message: String) extends RuntimeException {
  override def getMessage: String = message
}

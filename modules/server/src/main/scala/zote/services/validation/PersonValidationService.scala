package zote.services.validation

import zio.*
import zote.Ids.PersonId
import zote.db.repositories.PersonRepository
import zote.dto.form.PersonForm
import zote.exceptions.ValidationException

trait PersonValidationService {
  def validateForCreate(personForm: PersonForm.Raw): Task[PersonForm]
  def validateForUpdate(id: PersonId, personForm: PersonForm.Raw): Task[PersonForm]
}

case class PersonValidationServiceImpl(
    private val personRepository: PersonRepository,
) extends PersonValidationService {
  override def validateForCreate(personForm: PersonForm.Raw): Task[PersonForm] = {
    for {
      personForm <- personForm.validate
        .mapError(ValidationException.apply)
        .toZIOAssociative
      maybePersonEntity <- personRepository.findByName(personForm.name)
      _                 <- ZIO.when(maybePersonEntity.exists(_.name == personForm.name)) {
        notFound(personForm)
      }
    } yield personForm
  }

  override def validateForUpdate(id: PersonId, personForm: PersonForm.Raw): Task[PersonForm] = {
    for {
      personForm <- personForm.validate
        .mapError(ValidationException.apply)
        .toZIOAssociative
      maybePersonEntity <- personRepository.findByName(personForm.name)
      _ <- ZIO.when(maybePersonEntity.exists(person => person.id != id && person.name == personForm.name)) {
        notFound(personForm)
      }
    } yield personForm
  }

  inline private def notFound(personForm: PersonForm): Task[Unit] = {
    ZIO.fail(ValidationException(s"name ${personForm.name} already exists"))
  }

}

object PersonValidationServiceImpl {
  lazy val layer = ZLayer.derive[PersonValidationServiceImpl]
}

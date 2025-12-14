package zote.services.validation

import zio.*
import zote.Ids.UserId
import zote.db.repositories.UserRepository
import zote.dto.form.UserForm
import zote.exceptions.ValidationException

trait UserValidationService {
  def validateForCreate(userForm: UserForm.Raw): Task[UserForm]
  def validateForUpdate(id: UserId, userForm: UserForm.Raw): Task[UserForm]
}

case class UserValidationServiceImpl(
    private val userRepository: UserRepository,
) extends UserValidationService {
  override def validateForCreate(userForm: UserForm.Raw): Task[UserForm] = {
    for {
      userForm <- userForm.validate
        .mapError(ValidationException.apply)
        .toZIOAssociative
      maybeUserEntity <- userRepository.findByName(userForm.name)
      _               <- ZIO.when(maybeUserEntity.exists(_.name == userForm.name)) {
        notFound(userForm)
      }
    } yield userForm
  }

  override def validateForUpdate(id: UserId, userForm: UserForm.Raw): Task[UserForm] = {
    for {
      userForm <- userForm.validate
        .mapError(ValidationException.apply)
        .toZIOAssociative
      maybeUserEntity <- userRepository.findByName(userForm.name)
      _               <- ZIO.when(maybeUserEntity.exists(user => user.id != id && user.name == userForm.name)) {
        notFound(userForm)
      }
    } yield userForm
  }

  inline private def notFound(userForm: UserForm): Task[Unit] = {
    ZIO.fail(ValidationException(s"name ${userForm.name} already exists"))
  }

}

object UserValidationServiceImpl {
  lazy val layer = ZLayer.derive[UserValidationServiceImpl]
}

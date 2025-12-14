package zote.services

import zio.*
import zote.Ids.UserId
import zote.db.QuillContext
import zote.db.model.UserEntity
import zote.db.repositories.{NoteUserRepository, UserRepository}
import zote.dto.User
import zote.dto.form.UserForm

trait UserService {
  def getAll: Task[List[User]]

  def getById(id: UserId): Task[User]

  def create(userForm: UserForm): Task[User]

  def update(id: UserId, userForm: UserForm): Task[User]

  def delete(id: UserId): Task[Unit]
}

case class UserServiceImpl(
    private val userRepository: UserRepository,
    private val noteUserRepository: NoteUserRepository,
    private val quillContext: QuillContext,
) extends UserService {

  import quillContext.*

  override def getAll: Task[List[User]] = transaction {
    userRepository.findAll.flatMap { userEntities =>
      ZIO.foreachPar(userEntities)(toUser)
    }
  }

  override def getById(id: UserId): Task[User] = transaction {
    userRepository.getById(id).flatMap(toUser)
  }

  override def create(userForm: UserForm): Task[User] = transaction {
    for {
      userEntity <- toUserEntity(userForm)
      userEntity <- userRepository.upsert(userEntity)
      user       <- toUser(userEntity)
    } yield user
  }

  override def update(id: UserId, userForm: UserForm): Task[User] =
    transaction {
      for {
        userEntity <- userRepository.getById(id)
        userEntity <- toUserEntity(userForm, userEntity)
        userEntity <- userRepository.upsert(userEntity)
        user       <- toUser(userEntity)
      } yield user
    }

  inline private def toUserEntity(
      userForm: UserForm,
      inline userEntity: UserEntity | Unit = (),
  ): Task[UserEntity] = {
    inline userEntity match {
      case userEntity: UserEntity => ZIO.succeed(userEntity.copy(name = userForm.name))
      case _                      => ZIO.succeed(UserEntity(name = userForm.name))
    }
  }

  override def delete(id: UserId): Task[Unit] = transaction {
    for {
      _                <- userRepository.getById(id)
      noteUserEntities <- noteUserRepository.findAllByUserId(id)
      _                <- ZIO.foreachDiscard(noteUserEntities) { noteUserEntity =>
        noteUserRepository.delete(noteUserEntity.noteId, noteUserEntity.userId)
      }
      _ <- userRepository.delete(id)
    } yield ()
  }

  private def toUser(userEntity: UserEntity) = {
    ZIO.succeed {
      User(
        id = userEntity.id,
        name = userEntity.name,
      )
    }
  }
}

object UserServiceImpl {
  lazy val layer = ZLayer.derive[UserServiceImpl]
}

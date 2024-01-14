package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.UserEntity
import zote.db.repositories.UserRepository
import zote.dto.{User, UserForm}
import zote.exceptions.NotFoundException

trait UserService {
    def getAll: Task[Seq[User]]

    def getById(id: Long): Task[User]

    def create(userForm: UserForm): Task[User]

    def update(id: Long, userForm: UserForm): Task[User]

    def delete(id: Long): Task[Unit]
}

object UserService {
    lazy val layer = ZLayer.derive[UserServiceImpl]
}

case class UserServiceImpl(
    private val userRepository: UserRepository,
    private val quillContext: QuillContext
) extends UserService {

    import quillContext.postgres.*

    override def getAll: Task[Seq[User]] = transaction {
        userRepository.findAll.map(toDtos)
    }

    override def getById(id: Long): Task[User] = transaction {
        getEntityById(id).map(toDto)
    }

    override def create(userForm: UserForm): Task[User] = transaction {
        for {
            _ <- UserForm.validateZIO(userForm)
            userEntity <- ZIO.succeed {
                UserEntity(name = userForm.name)
            }
            id <- userRepository.save(userEntity)
            user <- getById(id)
        } yield user
    }

    override def update(id: Long, userForm: UserForm): Task[User] = transaction {
        for {
            _ <- UserForm.validateZIO(userForm)
            userEntity <- getEntityById(id)
            userEntity <- ZIO.succeed {
                userEntity
                    .modify(_.name).setTo(userForm.name)
            }
            id <- userRepository.save(userEntity)
            user <- getById(id)
        } yield user
    }

    override def delete(id: Long): Task[Unit] = transaction {
        for {
            _ <- getEntityById(id)
            _ <- userRepository.delete(id)
        } yield ()
    }

    private def getEntityById(id: Long): Task[UserEntity] = {
        userRepository.findById(id).flatMap {
            case Some(user) => ZIO.succeed(user)
            case None => ZIO.fail(NotFoundException(s"User id: $id not found"))
        }
    }

    private def toDto(userEntity: UserEntity): User = {
        User(
            id = userEntity.id,
            name = userEntity.name,
        )
    }

    private def toDtos(userEntities: Seq[UserEntity]): Seq[User] = {
        userEntities.map(toDto)
    }
}

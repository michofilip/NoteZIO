package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.UserEntity
import zote.db.repositories.{NoteUserRepository, UserRepository}
import zote.dto.{User, UserForm}
import zote.exceptions.NotFoundException

trait UserService {
    def getAll: Task[Seq[User]]

    def getById(id: Long): Task[User]

    def create(userForm: UserForm): Task[User]

    def update(id: Long, userForm: UserForm): Task[User]

    def delete(id: Long): Task[Unit]

    def getUsersByNoteIds(noteIds: Seq[Long]): Task[Map[Long, List[User]]]

    def validateUsersExist(userIds: Set[Long]): Task[Unit]
}

object UserService {
    lazy val layer = ZLayer.derive[UserServiceImpl]
}

case class UserServiceImpl(
    private val userRepository: UserRepository,
    private val noteUserRepository: NoteUserRepository,
    private val quillContext: QuillContext
) extends UserService {

    import quillContext.postgres.*

    override def getAll: Task[Seq[User]] = transaction {
        userRepository.findAll.flatMap(toDtos)
    }

    override def getById(id: Long): Task[User] = transaction {
        getEntityById(id).flatMap(toDto)
    }

    override def create(userForm: UserForm): Task[User] = upsert(userForm) {
        ZIO.succeed {
            UserEntity(name = userForm.name)
        }
    }

    override def update(id: Long, userForm: UserForm): Task[User] = upsert(userForm) {
        getEntityById(id).map(_
            .modify(_.name).setTo(userForm.name)
        )
    }

    private def upsert(userForm: UserForm)(f: => Task[UserEntity]): Task[User] = transaction {
        for {
            _ <- UserForm.validateZIO(userForm)
            userEntity <- f
            userEntity <- userRepository.upsert(userEntity)
            user <- toDto(userEntity)
        } yield user
    }

    override def delete(id: Long): Task[Unit] = transaction {
        for {
            _ <- getEntityById(id)
            _ <- userRepository.delete(id)
        } yield ()
    }

    override def getUsersByNoteIds(noteIds: Seq[Long]): Task[Map[Long, List[User]]] = {
        for {
            userEntitiesWithNoteIds <- noteUserRepository.findUsersWithNoteIds(noteIds)
            userEntities <- ZIO.succeed(userEntitiesWithNoteIds.map(_._1).distinct)
            userById <- toDtos(userEntities).map(_.map(u => u.id -> u).toMap)
            usersByNoteId <- ZIO.succeed {
                userEntitiesWithNoteIds.groupMap { case (_, noteId) =>
                    noteId
                } { case (userEntity, _) =>
                    userById(userEntity.id)
                }
            }
        } yield usersByNoteId
    }

    override def validateUsersExist(userIds: Set[Long]): Task[Unit] = {
        for {
            existingUserIds <- userRepository.findExistingIds(userIds)
            missingUserIds <- ZIO.succeed(userIds -- existingUserIds)
            _ <- ZIO.unless(missingUserIds.isEmpty) {
                ZIO.fail(NotFoundException(s"Users ids: ${missingUserIds.mkString(", ")} not found"))
            }
        } yield ()
    }

    private def getEntityById(id: Long): Task[UserEntity] = {
        userRepository.findById(id).someOrFail(NotFoundException(s"User id: $id not found"))
    }

    private def toDtos(userEntities: Seq[UserEntity]): Task[List[User]] = {
        ZIO.succeed {
            userEntities.map { userEntity =>
                User(
                    id = userEntity.id,
                    name = userEntity.name,
                )
            }.toList
        }
    }

    private def toDto(userEntity: UserEntity): Task[User] = {
        toDtos(List(userEntity)).map(_.head)
    }
}

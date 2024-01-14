package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.{NoteEntity, UserEntity}
import zote.db.repositories.{NoteRepository, UserRepository}
import zote.dto.{Note, NoteForm, User}
import zote.exceptions.NotFoundException

trait NoteService {
    def getAll: Task[Seq[Note]]

    def getById(id: Long): Task[Note]

    def create(noteForm: NoteForm): Task[Note]

    def update(id: Long, noteForm: NoteForm): Task[Note]

    def delete(id: Long): Task[Unit]
}

object NoteService {
    lazy val layer = ZLayer.derive[NoteServiceImpl]
}

case class NoteServiceImpl(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val quillContext: QuillContext
) extends NoteService {

    import quillContext.postgres.*

    override def getAll: Task[Seq[Note]] = transaction {
        noteRepository.findAll.map(toDtos)
    }

    override def getById(id: Long): Task[Note] = transaction {
        getEntityById(id).map(toDto)
    }

    override def create(noteForm: NoteForm): Task[Note] = transaction {
        for {
            _ <- NoteForm.validateZIO(noteForm)
            _ <- validateUsersExist(noteForm.userIds)
            noteEntity <- ZIO.succeed {
                NoteEntity(title = noteForm.title, message = noteForm.message, status = noteForm.status)
            }
            id <- noteRepository.save(noteEntity)
            _ <- noteRepository.updateNoteUsers(id, noteForm.userIds.toSeq)
            note <- getById(id)
        } yield note
    }

    override def update(id: Long, noteForm: NoteForm): Task[Note] = transaction {
        for {
            _ <- NoteForm.validateZIO(noteForm)
            _ <- validateUsersExist(noteForm.userIds)
            noteEntity <- getEntityById(id).map(_._1)
            noteEntity <- ZIO.succeed {
                noteEntity
                    .modify(_.title).setTo(noteForm.title)
                    .modify(_.message).setTo(noteForm.message)
                    .modify(_.status).setTo(noteForm.status)
            }
            id <- noteRepository.save(noteEntity)
            _ <- noteRepository.updateNoteUsers(id, noteForm.userIds.toSeq)
            note <- getById(id)
        } yield note
    }

    override def delete(id: Long): Task[Unit] = transaction {
        for {
            _ <- getEntityById(id)
            _ <- noteRepository.delete(id)
        } yield ()
    }

    private def getEntityById(id: Long): Task[(NoteEntity, Seq[UserEntity])] = {
        noteRepository.findById(id).flatMap {
            case Some(note) => ZIO.succeed(note)
            case None => ZIO.fail(NotFoundException(s"Note id: $id not found"))
        }
    }

    private def toDto(noteEntityWithUsers: (NoteEntity, Seq[UserEntity])): Note = {
        val (noteEntity, userEntities) = noteEntityWithUsers
        val users = userEntities.map { userEntity =>
            User(
                id = userEntity.id,
                name = userEntity.name
            )
        }

        Note(
            id = noteEntity.id,
            message = noteEntity.message,
            title = noteEntity.title,
            status = noteEntity.status,
            users = users
        )
    }

    private def toDtos(noteEntitiesWithUsers: Seq[(NoteEntity, Seq[UserEntity])]): Seq[Note] = {
        noteEntitiesWithUsers.map(toDto)
    }

    private def validateUsersExist(userIds: Set[Long]): Task[Unit] = {
        for {
            missingIds <- userRepository.findMissing(userIds)
            _ <- ZIO.unless(missingIds.isEmpty)(ZIO.fail(NotFoundException(s"Users ids: ${missingIds.mkString(", ")} not found")))
        } yield ()
    }
}

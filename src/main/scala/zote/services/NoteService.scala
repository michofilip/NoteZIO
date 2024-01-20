package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NoteEntity
import zote.db.repositories.{NoteRepository, NoteUserRepository}
import zote.dto.{Note, NoteForm}
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
    private val noteUserRepository: NoteUserRepository,
    private val userService: UserService,
    private val quillContext: QuillContext
) extends NoteService {

    import quillContext.postgres.*

    override def getAll: Task[Seq[Note]] = transaction {
        noteRepository.findAll.flatMap(toDtos)
    }

    override def getById(id: Long): Task[Note] = transaction {
        getEntityById(id).flatMap(toDto)
    }

    override def create(noteForm: NoteForm): Task[Note] = transaction {
        for {
            _ <- NoteForm.validateZIO(noteForm)
            _ <- userService.validateUsersExist(noteForm.userIds)
            noteEntity <- ZIO.succeed {
                NoteEntity(title = noteForm.title, message = noteForm.message, status = noteForm.status)
            }
            noteEntity <- noteRepository.save(noteEntity)
            _ <- noteUserRepository.updateNoteUsers(noteEntity.id, noteForm.userIds.toSeq)
            note <- toDto(noteEntity)
        } yield note
    }

    override def update(id: Long, noteForm: NoteForm): Task[Note] = transaction {
        for {
            _ <- NoteForm.validateZIO(noteForm)
            _ <- userService.validateUsersExist(noteForm.userIds)
            noteEntity <- getEntityById(id)
            noteEntity <- ZIO.succeed {
                noteEntity
                    .modify(_.title).setTo(noteForm.title)
                    .modify(_.message).setTo(noteForm.message)
                    .modify(_.status).setTo(noteForm.status)
            }
            noteEntity <- noteRepository.save(noteEntity)
            _ <- noteUserRepository.updateNoteUsers(noteEntity.id, noteForm.userIds.toSeq)
            note <- toDto(noteEntity)
        } yield note
    }

    override def delete(id: Long): Task[Unit] = transaction {
        for {
            _ <- getEntityById(id)
            _ <- noteRepository.delete(id)
        } yield ()
    }

    private def getEntityById(id: Long): Task[NoteEntity] = {
        noteRepository.findById(id).someOrFail(NotFoundException(s"Note id: $id not found"))
    }

    private def toDtos(noteEntities: Seq[NoteEntity]): Task[List[Note]] = {
        for {
            noteIds <- ZIO.succeed(noteEntities.map(_.id))
            usersByNoteId <- userService.getUsersByNoteIds(noteIds)
            notes <- ZIO.succeed {
                noteEntities.map { noteEntity =>
                    Note(
                        id = noteEntity.id,
                        message = noteEntity.message,
                        title = noteEntity.title,
                        status = noteEntity.status,
                        users = usersByNoteId.getOrElse(noteEntity.id, List.empty)
                    )
                }.toList
            }
        } yield notes
    }

    private def toDto(noteEntity: NoteEntity): Task[Note] = {
        toDtos(List(noteEntity)).map(_.head)
    }
}

package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NotePersonEntity}
import zote.db.repositories.{NotePersonRepository, NoteRepository}
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
    private val noteUserRepository: NotePersonRepository,
    private val personService: PersonService,
    private val quillContext: QuillContext
) extends NoteService {

    import quillContext.postgres.*

    override def getAll: Task[Seq[Note]] = transaction {
        noteRepository.findAll.flatMap(toDtos)
    }

    override def getById(id: Long): Task[Note] = transaction {
        getEntityById(id).flatMap(toDto)
    }

    override def create(noteForm: NoteForm): Task[Note] = upsert(noteForm) {
        ZIO.succeed {
            NoteEntity(title = noteForm.title, message = noteForm.message, status = noteForm.status)
        }
    }

    override def update(id: Long, noteForm: NoteForm): Task[Note] = upsert(noteForm) {
        getEntityById(id).map(_
            .modify(_.title).setTo(noteForm.title)
            .modify(_.message).setTo(noteForm.message)
            .modify(_.status).setTo(noteForm.status)
        )
    }

    private def upsert(noteForm: NoteForm)(f: => Task[NoteEntity]): Task[Note] = transaction {
        for {
            _ <- NoteForm.validateZIO(noteForm)
            _ <- personService.validatePersonsExist(noteForm.persons.map(_.personId))
            noteEntity <- f
            noteEntity <- noteRepository.upsert(noteEntity)
            notePersonEntities <- ZIO.succeed(
                noteForm.persons.map(person => NotePersonEntity(
                    noteId = noteEntity.id,
                    personId = person.personId,
                    owner = person.owner
                )).toList
            )
            _ <- noteUserRepository.updateNotePersons(notePersonEntities)
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
            notePersonsByNoteIds <- personService.getNotePersonsByNoteIds(noteIds)
            notes <- ZIO.succeed {
                noteEntities.map { noteEntity =>
                    Note(
                        id = noteEntity.id,
                        message = noteEntity.message,
                        title = noteEntity.title,
                        status = noteEntity.status,
                        persons = notePersonsByNoteIds.getOrElse(noteEntity.id, List.empty)
                    )
                }.toList
            }
        } yield notes
    }

    private def toDto(noteEntity: NoteEntity): Task[Note] = {
        toDtos(List(noteEntity)).map(_.head)
    }
}

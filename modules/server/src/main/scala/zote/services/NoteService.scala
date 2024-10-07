package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.*
import zote.dto
import zote.dto.*
import zote.dto.form.*
import zote.dto.validation.Validator

trait NoteService {
  def getAll: Task[List[NoteHeader]]

  def getById(id: Long): Task[Note]

  def create(noteForm: NoteForm): Task[Note]

  def update(id: Long, noteForm: NoteForm): Task[Note]

  def delete(id: Long): Task[Unit]
}

case class NoteServiceImpl(
    private val labelRepository: LabelRepository,
    private val labelService: LabelService,
    private val noteLabelRepository: NoteLabelRepository,
    private val notePersonRepository: NotePersonRepository,
    private val noteRepository: NoteRepository,
    private val personRepository: PersonRepository,
    private val personService: PersonService,
    private val quillContext: QuillContext
) extends NoteService {

  import quillContext.*

  override def getAll: Task[List[NoteHeader]] = transaction {
    noteRepository.findAll.flatMap { noteEntities =>
      ZIO.foreachPar(noteEntities)(toHeader)
    }
  }

  override def getById(id: Long): Task[Note] = transaction {
    noteRepository.getById(id).flatMap(toNote)
  }

  override def create(noteForm: NoteForm): Task[Note] = transaction {
    for {
      _ <- validateNote(noteForm)

      noteEntity <- noteRepository.upsert {
        NoteEntity(
          title = noteForm.title,
          message = noteForm.message,
          status = noteForm.status,
          parentId = noteForm.parentId
        )
      }

      _ <- updateDependencies(
        noteId = noteEntity.id,
        assignees = noteForm.assignees.toSeq,
        labelIds = noteForm.labels.toSeq
      )

      note <- toNote(noteEntity)
    } yield note
  }

  override def update(id: Long, noteForm: NoteForm): Task[Note] = transaction {
    for {
      _ <- validateNote(noteForm)

      noteEntity <- noteRepository.getById(id)
      noteEntity <- noteRepository.upsert {
        noteEntity
          .modify(_.title)
          .setTo(noteForm.title)
          .modify(_.message)
          .setTo(noteForm.message)
          .modify(_.status)
          .setTo(noteForm.status)
          .modify(_.parentId)
          .setTo(noteForm.parentId)
      }

      _ <- updateDependencies(
        noteId = noteEntity.id,
        assignees = noteForm.assignees.toSeq,
        labelIds = noteForm.labels.toSeq
      )

      note <- toNote(noteEntity)
    } yield note
  }

  override def delete(id: Long): Task[Unit] = transaction {
    for {
      _ <- noteRepository.getById(id)
      _ <- deleteDependencies(id) <&> detachChildren(id)
      _ <- noteRepository.delete(id)
    } yield ()
  }

  private def validateNote(noteForm: NoteForm) = {
    Validator.validateZIO(noteForm)
      <&> ZIO.foreachDiscard(noteForm.parentId)(noteRepository.getById)
      <&> ZIO.foreachParDiscard(noteForm.assignees.map(_.personId))(
        personRepository.getById
      )
      <&> ZIO.foreachParDiscard(noteForm.labels)(labelRepository.getById)
  }

  private def deleteDependencies(noteId: Long) = {
    updateDependencies(noteId, Seq.empty, Seq.empty)
  }

  private def detachChildren(id: Long): Task[Unit] = {
    for {
      noteEntities <- noteRepository.findAllByParentId(id)
      noteEntities <- ZIO.succeed(
        noteEntities.map(_.modify(_.parentId).setTo(None))
      )
      _ <- ZIO.foreachParDiscard(noteEntities)(noteRepository.upsert)
    } yield ()
  }

  private def updateDependencies(
      noteId: Long,
      assignees: Seq[NotePersonForm],
      labelIds: Seq[Long]
  ) = {
    updateNotePersons(noteId, assignees) <&> updateNoteLabels(noteId, labelIds)
  }

  private def updateNotePersons(
      noteId: Long,
      notePersons: Seq[NotePersonForm]
  ) = {
    for {
      currentNotePersonEntities <- notePersonRepository
        .findAllByNoteId(noteId)
        .map(_.map(np => (np.personId, np.role) -> np).toMap)
      newNotePersonEntities = notePersons.map { np =>
        (np.personId, np.role) -> NotePersonEntity(
          noteId = noteId,
          personId = np.personId,
          role = np.role
        )
      }.toMap

      currentVsNew =
        (currentNotePersonEntities.keySet ++ newNotePersonEntities.keySet).toList
          .map { key =>
            (currentNotePersonEntities.get(key), newNotePersonEntities.get(key))
          }

      notePersonEntitiesToCreate = currentVsNew.collect {
        case (None, Some(np)) => np
      }
      notePersonEntitiesToDelete = currentVsNew.collect {
        case (Some(np), None) => np
      }

      _ <- notePersonRepository
        .delete(notePersonEntitiesToDelete)
        .unless(notePersonEntitiesToDelete.isEmpty)
      _ <- notePersonRepository
        .insert(notePersonEntitiesToCreate)
        .unless(notePersonEntitiesToCreate.isEmpty)
    } yield ()
  }

  private def updateNoteLabels(noteId: Long, labelIds: Seq[Long]) = {
    for {
      currentNoteLabelEntities <- noteLabelRepository
        .findAllByNoteId(noteId)
        .map(_.map(np => np.labelId -> np).toMap)
      newNoteLabelEntities = labelIds.map { labelId =>
        labelId -> NoteLabelEntity(
          noteId = noteId,
          labelId = labelId
        )
      }.toMap

      currentVsNew =
        (currentNoteLabelEntities.keySet ++ newNoteLabelEntities.keySet).toList
          .map { key =>
            (currentNoteLabelEntities.get(key), newNoteLabelEntities.get(key))
          }

      noteLabelEntitiesToCreate = currentVsNew.collect {
        case (None, Some(nl)) => nl
      }
      noteLabelEntitiesToDelete = currentVsNew.collect {
        case (Some(nl), None) => nl
      }

      _ <- noteLabelRepository
        .delete(noteLabelEntitiesToDelete)
        .unless(noteLabelEntitiesToDelete.isEmpty)
      _ <- noteLabelRepository
        .insert(noteLabelEntitiesToCreate)
        .unless(noteLabelEntitiesToCreate.isEmpty)
    } yield ()
  }

  private def toHeader(noteEntity: NoteEntity) = {
    getLabels(noteEntity).map { labels =>
      NoteHeader(
        id = noteEntity.id,
        title = noteEntity.title,
        status = noteEntity.status,
        labels = labels
      )
    }
  }

  private def toNote(noteEntity: NoteEntity) = {
    {
      toHeader(noteEntity)
        <&> getParentNote(noteEntity)
        <&> getChildrenNotes(noteEntity)
        <&> getAssignees(noteEntity)
    }.map { case (header, parentNote, childrenNotes, assignees) =>
      Note(
        header = header,
        parentNote = parentNote,
        childrenNotes = childrenNotes,
        message = noteEntity.message,
        assignees = assignees
      )
    }
  }

  private def getLabels(noteEntity: NoteEntity): Task[Option[List[Label]]] = {
    noteLabelRepository.findAllByNoteId(noteEntity.id).flatMap {
      noteLabelEntities =>
        val labelIds = noteLabelEntities.map(_.labelId)
        ZIO
          .foreachPar(labelIds)(labelService.getById)
          .unless(noteLabelEntities.isEmpty)
    }
  }

  private def getParentNote(
      noteEntity: NoteEntity
  ): Task[Option[NoteHeader]] = {
    ZIO.foreach(noteEntity.parentId) { parentId =>
      noteRepository.getById(parentId).flatMap(toHeader)
    }
  }

  private def getChildrenNotes(
      noteEntity: NoteEntity
  ): Task[Option[List[NoteHeader]]] = {
    noteRepository.findAllByParentId(noteEntity.id).flatMap { noteEntities =>
      ZIO.foreachPar(noteEntities)(toHeader).unless(noteEntities.isEmpty)
    }
  }

  private def getAssignees(
      noteEntity: NoteEntity
  ): Task[Option[List[NotePerson]]] = {
    notePersonRepository.findAllByNoteId(noteEntity.id).flatMap {
      notePersonEntities =>
        ZIO.unless(notePersonEntities.isEmpty) {
          val personIdToRoles =
            notePersonEntities.groupMap(_.personId)(_.role).toList
          ZIO.foreachPar(personIdToRoles) { case (personId, roles) =>
            personService.getById(personId).map { person =>
              NotePerson(
                person = person,
                roles = roles
              )
            }
          }
        }
    }
  }
}

object NoteServiceImpl {
  lazy val layer = ZLayer.derive[NoteServiceImpl]
}

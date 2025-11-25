package zote.services

import zio.*
import zote.Ids.{LabelId, NoteId}
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.*
import zote.dto
import zote.dto.*
import zote.dto.form.*
import zote.dto.validation.Validator

trait NoteService {
  def getAll: Task[List[NoteHeader]]

  def getById(id: NoteId): Task[Note]

  def create(noteForm: NoteForm): Task[Note]

  def update(id: NoteId, noteForm: NoteForm): Task[Note]

  def delete(id: NoteId): Task[Unit]
}

case class NoteServiceImpl(
    private val labelRepository: LabelRepository,
    private val labelService: LabelService,
    private val noteLabelRepository: NoteLabelRepository,
    private val notePersonRepository: NotePersonRepository,
    private val noteRepository: NoteRepository,
    private val personRepository: PersonRepository,
    private val personService: PersonService,
    private val quillContext: QuillContext,
) extends NoteService {

  import quillContext.*

  override def getAll: Task[List[NoteHeader]] = transaction {
    noteRepository.findAll.flatMap { noteEntities =>
      ZIO.foreachPar(noteEntities)(toHeader)
    }
  }

  override def getById(id: NoteId): Task[Note] = transaction {
    noteRepository.getById(id).flatMap(toNote)
  }

  override def create(noteForm: NoteForm): Task[Note] = transaction {
    for {
      _          <- validateNote(noteForm)
      noteEntity <- toNoteEntity(noteForm)
      noteEntity <- noteRepository.upsert(noteEntity)
      _          <- updateDependencies(noteEntity.id, noteForm)
      note       <- toNote(noteEntity)
    } yield note
  }

  override def update(id: NoteId, noteForm: NoteForm): Task[Note] = transaction {
    for {
      _          <- validateNote(noteForm)
      noteEntity <- noteRepository.getById(id)
      noteEntity <- toNoteEntity(noteForm, noteEntity)
      noteEntity <- noteRepository.upsert(noteEntity)
      _          <- updateDependencies(noteEntity.id, noteForm)
      note       <- toNote(noteEntity)
    } yield note
  }

  inline private def toNoteEntity(noteForm: NoteForm, inline noteEntity: NoteEntity | Unit = ()): Task[NoteEntity] = {
    inline noteEntity match {
      case noteEntity: NoteEntity =>
        ZIO.succeed {
          noteEntity.copy(
            title = noteForm.title,
            message = noteForm.message,
            status = noteForm.status,
            parentId = noteForm.parentId,
          )
        }
      case _ =>
        ZIO.succeed {
          NoteEntity(
            title = noteForm.title,
            message = noteForm.message,
            status = noteForm.status,
            parentId = noteForm.parentId,
          )
        }
    }
  }

  override def delete(id: NoteId): Task[Unit] = transaction {
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
        personRepository.getById,
      )
      <&> ZIO.foreachParDiscard(noteForm.labels)(labelRepository.getById)
  }

  private def deleteDependencies(noteId: NoteId): Task[Unit] = {
    val deleteNoteLabelEntities = noteLabelRepository.findAllByNoteId(noteId).flatMap { noteLabelEntities =>
      ZIO.foreachParDiscard(noteLabelEntities) { noteLabelEntity =>
        noteLabelRepository.delete(noteLabelEntity.noteId, noteLabelEntity.labelId)
      }
    }

    val deleteNotePersonEntities = notePersonRepository.findAllByNoteId(noteId).flatMap { notePersonEntities =>
      ZIO.foreachParDiscard(notePersonEntities) { notePersonEntity =>
        notePersonRepository.delete(notePersonEntity.noteId, notePersonEntity.personId)
      }
    }

    deleteNoteLabelEntities <&> deleteNotePersonEntities
  }

  private def detachChildren(noteId: NoteId): Task[Unit] = {
    for {
      noteEntities <- noteRepository.findAllByParentNoteId(noteId)
      noteEntities <- ZIO.succeed(noteEntities.map(_.copy(parentId = None)))
      _            <- ZIO.foreachParDiscard(noteEntities)(noteRepository.upsert)
    } yield ()
  }

  private def updateDependencies(
      noteId: NoteId,
      noteForm: NoteForm,
  ): Task[Unit] = {
    updateNotePersons(noteId, noteForm.assignees.toSeq) <&> updateNoteLabels(noteId, noteForm.labels.toSeq)
  }

  private def updateNotePersons(
      noteId: NoteId,
      notePersons: Seq[NotePersonForm],
  ): Task[Unit] = {
    for {
      currentNotePersonEntities <- notePersonRepository
        .findAllByNoteId(noteId)
        .map(_.map(notePersonEntity => (notePersonEntity.personId, notePersonEntity.role) -> notePersonEntity).toMap)
      newNotePersonEntities = notePersons.map { notePersonForm =>
        (notePersonForm.personId, notePersonForm.role) -> NotePersonEntity(
          noteId = noteId,
          personId = notePersonForm.personId,
          role = notePersonForm.role,
        )
      }.toMap

      currentVsNew = (currentNotePersonEntities.keySet ++ newNotePersonEntities.keySet).toList.map { key =>
        (currentNotePersonEntities.get(key), newNotePersonEntities.get(key))
      }

      _ <- ZIO.foreachParDiscard(currentVsNew) {
        case (Some(current), Some(entity)) if current.role != entity.role =>
          notePersonRepository.upsert(entity)
        case (None, Some(entity)) =>
          notePersonRepository.upsert(entity)
        case (Some(entity), None) =>
          notePersonRepository.delete(entity.noteId, entity.personId)
        case _ =>
          ZIO.unit
      }
    } yield ()
  }

  private def updateNoteLabels(noteId: NoteId, labelIds: Seq[LabelId]): Task[Unit] = {
    for {
      currentNoteLabelEntities <- noteLabelRepository
        .findAllByNoteId(noteId)
        .map(_.map(noteLabelEntity => noteLabelEntity.labelId -> noteLabelEntity).toMap)
      newNoteLabelEntities = labelIds.map { labelId =>
        labelId -> NoteLabelEntity(
          noteId = noteId,
          labelId = labelId,
        )
      }.toMap

      currentVsNew =
        (currentNoteLabelEntities.keySet ++ newNoteLabelEntities.keySet).toList.map { key =>
          (currentNoteLabelEntities.get(key), newNoteLabelEntities.get(key))
        }

      _ <- ZIO.foreachParDiscard(currentVsNew) {
        case (None, Some(entity)) =>
          noteLabelRepository.insert(entity)
        case (Some(entity), None) =>
          noteLabelRepository.delete(entity.noteId, entity.labelId)
        case _ =>
          ZIO.unit
      }
    } yield ()
  }

  private def toHeader(noteEntity: NoteEntity) = {
    getLabels(noteEntity).map { labels =>
      NoteHeader(
        id = noteEntity.id,
        title = noteEntity.title,
        status = noteEntity.status,
        labels = labels,
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
        assignees = assignees,
      )
    }
  }

  private def getLabels(noteEntity: NoteEntity): Task[Option[List[Label]]] = {
    noteLabelRepository.findAllByNoteId(noteEntity.id).flatMap { noteLabelEntities =>
      val labelIds = noteLabelEntities.map(_.labelId)
      ZIO
        .foreachPar(labelIds)(labelService.getById)
        .unless(noteLabelEntities.isEmpty)
    }
  }

  private def getParentNote(
      noteEntity: NoteEntity,
  ): Task[Option[NoteHeader]] = {
    ZIO.foreach(noteEntity.parentId) { parentId =>
      noteRepository.getById(parentId).flatMap(toHeader)
    }
  }

  private def getChildrenNotes(
      noteEntity: NoteEntity,
  ): Task[Option[List[NoteHeader]]] = {
    noteRepository.findAllByParentNoteId(noteEntity.id).flatMap { noteEntities =>
      ZIO
        .foreachPar(noteEntities)(toHeader)
        .unless(noteEntities.isEmpty)
    }
  }

  private def getAssignees(
      noteEntity: NoteEntity,
  ): Task[Option[List[NotePerson]]] = {
    for {
      notePersonEntities <- notePersonRepository.findAllByNoteId(noteEntity.id)
      maybeNotePersons <- ZIO
        .foreachPar(notePersonEntities) { notePersonEntity =>
          personService.getById(notePersonEntity.personId).map { person =>
            NotePerson(
              person = person,
              role = notePersonEntity.role,
            )
          }
        }
        .unless(notePersonEntities.isEmpty)
    } yield maybeNotePersons
  }
}

object NoteServiceImpl {
  lazy val layer = ZLayer.derive[NoteServiceImpl]
}

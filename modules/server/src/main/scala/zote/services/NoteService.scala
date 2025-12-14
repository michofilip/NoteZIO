package zote.services

import zio.*
import zote.Ids.{LabelId, NoteId}
import zote.db.QuillContext
import zote.db.model.*
import zote.db.repositories.*
import zote.dto
import zote.dto.*
import zote.dto.form.*

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
    private val noteUserRepository: NoteUserRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val userService: UserService,
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
      noteEntity <- toNoteEntity(noteForm)
      noteEntity <- noteRepository.upsert(noteEntity)
      _          <- updateDependencies(noteEntity.id, noteForm)
      note       <- toNote(noteEntity)
    } yield note
  }

  override def update(id: NoteId, noteForm: NoteForm): Task[Note] = transaction {
    for {
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

  private def deleteDependencies(noteId: NoteId): Task[Unit] = {
    val deleteNoteLabelEntities = noteLabelRepository.findAllByNoteId(noteId).flatMap { noteLabelEntities =>
      ZIO.foreachParDiscard(noteLabelEntities) { noteLabelEntity =>
        noteLabelRepository.delete(noteLabelEntity.noteId, noteLabelEntity.labelId)
      }
    }

    val deleteNoteUserEntities = noteUserRepository.findAllByNoteId(noteId).flatMap { noteUserEntities =>
      ZIO.foreachParDiscard(noteUserEntities) { noteUserEntity =>
        noteUserRepository.delete(noteUserEntity.noteId, noteUserEntity.userId)
      }
    }

    deleteNoteLabelEntities <&> deleteNoteUserEntities
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
    updateNoteUsers(noteId, noteForm.assignees.toSeq) <&> updateNoteLabels(noteId, noteForm.labels.toSeq)
  }

  private def updateNoteUsers(
      noteId: NoteId,
      noteUserForms: Seq[NoteUserForm],
  ): Task[Unit] = {
    for {
      currentNoteUserEntities <- noteUserRepository
        .findAllByNoteId(noteId)
        .map(_.map(noteUserEntity => (noteUserEntity.userId, noteUserEntity.role) -> noteUserEntity).toMap)
      newNoteUserEntities = noteUserForms.map { noteUserForm =>
        (noteUserForm.userId, noteUserForm.role) -> NoteUserEntity(
          noteId = noteId,
          userId = noteUserForm.userId,
          role = noteUserForm.role,
        )
      }.toMap

      currentVsNew = (currentNoteUserEntities.keySet ++ newNoteUserEntities.keySet).toList.map { key =>
        (currentNoteUserEntities.get(key), newNoteUserEntities.get(key))
      }

      _ <- ZIO.foreachParDiscard(currentVsNew) {
        case (Some(current), Some(entity)) if current.role != entity.role =>
          noteUserRepository.upsert(entity)
        case (None, Some(entity)) =>
          noteUserRepository.upsert(entity)
        case (Some(entity), None) =>
          noteUserRepository.delete(entity.noteId, entity.userId)
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
  ): Task[Option[List[NoteUser]]] = {
    for {
      noteUserEntities <- noteUserRepository.findAllByNoteId(noteEntity.id)
      maybeNoteUsers   <- ZIO
        .foreachPar(noteUserEntities) { noteUserEntity =>
          userService.getById(noteUserEntity.userId).map { user =>
            NoteUser(
              user = user,
              role = noteUserEntity.role,
            )
          }
        }
        .unless(noteUserEntities.isEmpty)
    } yield maybeNoteUsers
  }
}

object NoteServiceImpl {
  lazy val layer = ZLayer.derive[NoteServiceImpl]
}

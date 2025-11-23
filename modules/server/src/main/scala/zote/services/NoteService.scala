package zote.services

import com.softwaremill.quicklens.*
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
      _ <- validateNote(noteForm)

      noteEntity <- noteRepository.upsert {
        NoteEntity(
          title = noteForm.title,
          message = noteForm.message,
          status = noteForm.status,
          parentId = noteForm.parentId,
        )
      }

      _ <- updateDependencies(
        noteId = noteEntity.id,
        assignees = noteForm.assignees.toSeq,
        labelIds = noteForm.labels.toSeq,
      )

      note <- toNote(noteEntity)
    } yield note
  }

  override def update(id: NoteId, noteForm: NoteForm): Task[Note] = transaction {
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
        labelIds = noteForm.labels.toSeq,
      )

      note <- toNote(noteEntity)
    } yield note
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

  private def deleteDependencies(noteId: NoteId) = {
    updateDependencies(noteId, Seq.empty, Seq.empty)
  }

  private def detachChildren(noteId: NoteId): Task[Unit] = {
    for {
      noteEntities <- noteRepository.findAllByParentNoteId(noteId)
      noteEntities <- ZIO.succeed(
        noteEntities.map(_.modify(_.parentId).setTo(None)),
      )
      _ <- ZIO.foreachParDiscard(noteEntities)(noteRepository.upsert)
    } yield ()
  }

  private def updateDependencies(
      noteId: NoteId,
      assignees: Seq[NotePersonForm],
      labelIds: Seq[LabelId],
  ) = {
    updateNotePersons(noteId, assignees) <&> updateNoteLabels(noteId, labelIds)
  }

  private def updateNotePersons(
      noteId: NoteId,
      notePersons: Seq[NotePersonForm],
  ) = {
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

      notePersonEntitiesToCreate = currentVsNew.collect { case (None, Some(entity)) => entity }
      notePersonEntitiesToDelete = currentVsNew.collect { case (Some(entity), None) =>
        (entity.noteId, entity.personId)
      }

      _ <- notePersonRepository
        .deleteAll(notePersonEntitiesToDelete)
        .unless(notePersonEntitiesToDelete.isEmpty)
      _ <- notePersonRepository
        .insertAll(notePersonEntitiesToCreate)
        .unless(notePersonEntitiesToCreate.isEmpty)
    } yield ()
  }

  private def updateNoteLabels(noteId: NoteId, labelIds: Seq[LabelId]) = {
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

      noteLabelEntitiesToCreate = currentVsNew.collect { case (None, Some(entity)) => entity }
      noteLabelEntitiesToDelete = currentVsNew.collect { case (Some(entity), None) => (entity.noteId, entity.labelId) }

      _ <- noteLabelRepository
        .deleteAll(noteLabelEntitiesToDelete)
        .unless(noteLabelEntitiesToDelete.isEmpty)
      _ <- noteLabelRepository
        .insertAll(noteLabelEntitiesToCreate)
        .unless(noteLabelEntitiesToCreate.isEmpty)
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
      ZIO.foreachPar(noteEntities)(toHeader).unless(noteEntities.isEmpty)
    }
  }

  private def getAssignees(
      noteEntity: NoteEntity,
  ): Task[Option[List[NotePerson]]] = {
    notePersonRepository.findAllByNoteId(noteEntity.id).flatMap { notePersonEntities =>
      ZIO.unless(notePersonEntities.isEmpty) {
        val personIdToRoles = notePersonEntities.groupMap(_.personId)(_.role).toList
        ZIO.foreachPar(personIdToRoles) { case (personId, roles) =>
          personService.getById(personId).map { person =>
            NotePerson(
              person = person,
              roles = roles,
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

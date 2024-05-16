package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.{NoteEntity, NoteLabelEntity, NotePersonEntity}
import zote.db.repositories.*
import zote.dto.*

trait NoteService {
  def getAll: Task[List[NoteHeader]]

  def getById(id: Long): Task[Note]

  def create(noteForm: NoteForm): Task[Note]

  def update(id: Long, noteForm: NoteForm): Task[Note]

  def delete(id: Long): Task[Unit]
}

object NoteService {
  lazy val layer = ZLayer.derive[NoteServiceImpl]
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

  import quillContext.postgres.*

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
      _ <- NoteForm.validateZIO(noteForm)
      _ <- ZIO.foreachDiscard(noteForm.parentId)(noteRepository.getById)
      _ <- ZIO.foreachParDiscard(noteForm.assignees.map(_.personId))(personRepository.getById)
      _ <- ZIO.foreachParDiscard(noteForm.labels)(labelRepository.getById)
      noteEntity <- noteRepository.upsert {
        NoteEntity(
          title = noteForm.title,
          message = noteForm.message,
          status = noteForm.status,
          parentId = noteForm.parentId,
        )
      }

      updateNotePersonsFiber <- updateNotePersons(noteEntity.id, noteForm.assignees.toSeq).fork
      updateNoteLabelsFiber <- updateNoteLabels(noteEntity.id, noteForm.labels.toSeq).fork
      _ <- updateNotePersonsFiber.join
      _ <- updateNoteLabelsFiber.join

      note <- toNote(noteEntity)
    } yield note
  }

  override def update(id: Long, noteForm: NoteForm): Task[Note] = transaction {
    for {
      _ <- NoteForm.validateZIO(noteForm)
      _ <- ZIO.foreachDiscard(noteForm.parentId)(noteRepository.getById)
      _ <- ZIO.foreachParDiscard(noteForm.assignees.map(_.personId))(personRepository.getById)
      _ <- ZIO.foreachParDiscard(noteForm.labels)(labelRepository.getById)
      noteEntity <- noteRepository.getById(id)
      noteEntity <- noteRepository.upsert {
        noteEntity
          .modify(_.title).setTo(noteForm.title)
          .modify(_.message).setTo(noteForm.message)
          .modify(_.status).setTo(noteForm.status)
          .modify(_.parentId).setTo(noteForm.parentId)
      }

      updateNotePersonsFiber <- updateNotePersons(noteEntity.id, noteForm.assignees.toSeq).fork
      updateNoteLabelsFiber <- updateNoteLabels(noteEntity.id, noteForm.labels.toSeq).fork
      _ <- updateNotePersonsFiber.join
      _ <- updateNoteLabelsFiber.join

      note <- toNote(noteEntity)
    } yield note
  }

  override def delete(id: Long): Task[Unit] = transaction {
    for {
      _ <- noteRepository.getById(id)

      updateNotePersonsFiber <- updateNotePersons(id, Seq.empty).fork
      updateNoteLabelsFiber <- updateNoteLabels(id, Seq.empty).fork
      _ <- updateNotePersonsFiber.join
      _ <- updateNoteLabelsFiber.join

      _ <- noteRepository.delete(id)
    } yield ()
  }

  private def updateNotePersons(noteId: Long, notePersonIds: Seq[NotePersonForm]) = {
    for {
      currentNotePersonEntities <- notePersonRepository.findAllByNoteId(noteId)
        .map(_.map(np => np.personId -> np).toMap)
      newNotePersonEntities = notePersonIds.map { np =>
        np.personId -> NotePersonEntity(
          noteId = noteId,
          personId = np.personId,
          owner = np.owner
        )
      }.toMap

      currentVsNew = (currentNotePersonEntities.keySet ++ newNotePersonEntities.keySet).toList.map { key =>
        (currentNotePersonEntities.get(key), newNotePersonEntities.get(key))
      }

      notePersonEntitiesToCreate = currentVsNew.collect { case (None, Some(np)) => np }
      notePersonEntitiesToUpdate = currentVsNew.collect { case (Some(npCurrent), Some(npNew)) if npCurrent != npNew => npNew }
      notePersonEntitiesToDelete = currentVsNew.collect { case (Some(np), None) => np }

      _ <- notePersonRepository.insert(notePersonEntitiesToCreate).unless(notePersonEntitiesToCreate.isEmpty)
      _ <- notePersonRepository.update(notePersonEntitiesToUpdate).unless(notePersonEntitiesToUpdate.isEmpty)
      _ <- notePersonRepository.delete(notePersonEntitiesToDelete).unless(notePersonEntitiesToDelete.isEmpty)
    } yield ()
  }

  private def updateNoteLabels(noteId: Long, labelIds: Seq[Long]) = {
    for {
      currentNoteLabelEntities <- noteLabelRepository.findAllByNoteId(noteId)
        .map(_.map(np => np.labelId -> np).toMap)
      newNoteLabelEntities = labelIds.map { labelId =>
        labelId -> NoteLabelEntity(
          noteId = noteId,
          labelId = labelId,
        )
      }.toMap

      currentVsNew = (currentNoteLabelEntities.keySet ++ newNoteLabelEntities.keySet).toList.map { key =>
        (currentNoteLabelEntities.get(key), newNoteLabelEntities.get(key))
      }

      noteLabelEntitiesToCreate = currentVsNew.collect { case (None, Some(nl)) => nl }
      noteLabelEntitiesToDelete = currentVsNew.collect { case (Some(nl), None) => nl }

      _ <- noteLabelRepository.insert(noteLabelEntitiesToCreate).unless(noteLabelEntitiesToCreate.isEmpty)
      _ <- noteLabelRepository.delete(noteLabelEntitiesToDelete).unless(noteLabelEntitiesToDelete.isEmpty)
    } yield ()
  }

  private def toHeader(noteEntity: NoteEntity) = {
    for {
      noteLabelEntities <- noteLabelRepository.findAllByNoteId(noteEntity.id)
      labels <- ZIO.foreachPar(noteLabelEntities.map(_.labelId))(labelService.getById).unless(noteLabelEntities.isEmpty)
    } yield {
      NoteHeader(
        id = noteEntity.id,
        title = noteEntity.title,
        status = noteEntity.status,
        labels = labels
      )
    }
  }

  private def toNote(noteEntity: NoteEntity) = {
    for {
      headerFiber <- toHeader(noteEntity).fork
      parentNoteFiber <- getParentNote(noteEntity).fork
      childrenNotesFiber <- getChildrenNotes(noteEntity).fork
      assigneesFiber <- getAssignees(noteEntity).fork

      header <- headerFiber.join
      parentNote <- parentNoteFiber.join
      childrenNotes <- childrenNotesFiber.join
      assignees <- assigneesFiber.join
    } yield {
      Note(
        header = header,
        parentNote = parentNote,
        childrenNotes = childrenNotes,
        message = noteEntity.message,
        assignees = assignees,
      )
    }
  }

  private def getParentNote(noteEntity: NoteEntity): Task[Option[NoteHeader]] = {
    ZIO.foreach(noteEntity.parentId) { parentId =>
      noteRepository.getById(parentId).flatMap(toHeader)
    }
  }

  private def getChildrenNotes(noteEntity: NoteEntity): Task[Option[List[NoteHeader]]] = {
    noteRepository.findAllByParentId(noteEntity.id).flatMap { noteEntities =>
      ZIO.foreachPar(noteEntities)(toHeader).unless(noteEntities.isEmpty)
    }
  }

  private def getAssignees(noteEntity: NoteEntity): Task[Option[List[NotePerson]]] = {
    notePersonRepository.findAllByNoteId(noteEntity.id).flatMap { notePersonEntities =>
      ZIO.foreachPar(notePersonEntities)(toAssignee).unless(notePersonEntities.isEmpty)
    }
  }

  private def toAssignee(notePersonEntity: NotePersonEntity) = {
    for {
      person <- personService.getById(notePersonEntity.personId)
    } yield {
      NotePerson(
        person = person,
        owner = notePersonEntity.owner,
      )
    }
  }
}

package zote.services

import zio.*
import zote.db.model.NotePersonEntity
import zote.db.repositories.NotePersonRepository
import zote.dto.{NotePerson, NotePersonId}
import zote.utils.ZIOUtils.*

trait NotePersonService {
    def updateNotePersons(noteId: Long, notePersonIds: Seq[NotePersonId]): Task[Unit]

    def getNotePersonsByNoteIds(noteIds: Seq[Long]): Task[Map[Long, List[NotePerson]]]
}

object NotePersonService {
    lazy val layer = ZLayer.derive[NotePersonServiceImpl]
}

case class NotePersonServiceImpl(
    private val notePersonRepository: NotePersonRepository,
    private val personService: PersonService,
) extends NotePersonService {
    override def updateNotePersons(noteId: Long, notePersonIds: Seq[NotePersonId]): Task[Unit] = {
        for {
            currentNotePersonEntities <- notePersonRepository.findByNoteId(noteId).toMap(np => (np.noteId, np.personId))
            newNotePersonEntities <- notePersonIds.map { notePersonId =>
                NotePersonEntity(
                    noteId = noteId,
                    personId = notePersonId.personId,
                    owner = notePersonId.owner
                )
            }.asZIO.toMap(np => (np.noteId, np.personId))

            currentVsNew <- (currentNotePersonEntities.keySet ++ newNotePersonEntities.keySet).toList.map { key =>
                (currentNotePersonEntities.get(key), newNotePersonEntities.get(key))
            }.asZIO

            notePersonEntitiesToAdd <- currentVsNew.collect { case (None, Some(np)) => np }.asZIO
            notePersonEntitiesToUpdate <- currentVsNew.collect { case (Some(npCurrent), Some(npNew)) if npCurrent != npNew => npNew }.asZIO
            notePersonEntitiesToRemove <- currentVsNew.collect { case (Some(np), None) => np }.asZIO

            _ <- notePersonRepository.insert(notePersonEntitiesToAdd).unless(notePersonEntitiesToAdd.isEmpty)
            _ <- notePersonRepository.update(notePersonEntitiesToUpdate).unless(notePersonEntitiesToUpdate.isEmpty)
            _ <- notePersonRepository.delete(notePersonEntitiesToRemove).unless(notePersonEntitiesToRemove.isEmpty)
        } yield ()
    }

    override def getNotePersonsByNoteIds(noteIds: Seq[Long]): Task[Map[Long, List[NotePerson]]] = {
        for {
            personNoteEntities <- notePersonRepository.findByNoteIdIn(noteIds)
            personById <- personNoteEntities.map(_.personId).distinct.asZIO
                .flatMap(personService.getByIdIn)
                .toMap(_.id)
            notePersonsByNoteIds <- personNoteEntities.groupMap(_.noteId) { notePersonEntity =>
                NotePerson(
                    person = personById(notePersonEntity.personId),
                    owner = notePersonEntity.owner
                )
            }.asZIO
        } yield notePersonsByNoteIds
    }
}
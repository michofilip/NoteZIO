package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.{NotePersonEntity, PersonEntity}

trait NotePersonRepository {

    def findByNoteIds(noteIds: Seq[Long]): Task[List[(NotePersonEntity, PersonEntity)]]

    def updateNotePersons(notePersonEntities: Seq[NotePersonEntity]): Task[Unit]
}

object NotePersonRepository {
    lazy val layer = ZLayer.derive[NotePersonRepositoryImpl]
}

case class NotePersonRepositoryImpl(
    private val quillContext: QuillContext
) extends NotePersonRepository {

    import quillContext.postgres.*

    override def findByNoteIds(noteIds: Seq[Long]): Task[List[(NotePersonEntity, PersonEntity)]] = transaction {
        run {
            query[NotePersonEntity]
                .join(query[PersonEntity]).on((np, p) => np.personId == p.id)
                .filter { case (np, _) => liftQuery(noteIds).contains(np.noteId) }
        }
    }

    override def updateNotePersons(notePersonEntities: Seq[NotePersonEntity]): Task[Unit] = transaction {
        for {
            noteIds <- ZIO.succeed(notePersonEntities.map(_.noteId))

            newNotePersonEntities <- ZIO.succeed(notePersonEntities.map(np => (np.noteId, np.personId) -> np).toMap)
            currentNotePersonEntities <- run(query[NotePersonEntity].filter(np => liftQuery(noteIds).contains(np.noteId)))
                .map(_.map(np => (np.noteId, np.personId) -> np).toMap)

            currentVsNew <- ZIO.succeed {
                (currentNotePersonEntities.keySet ++ newNotePersonEntities.keySet).toList.map { key =>
                    (currentNotePersonEntities.get(key), newNotePersonEntities.get(key))
                }
            }

            notePersonEntitiesToAdd <- ZIO.succeed {
                currentVsNew.collect { case (None, Some(np)) => np }
            }

            notePersonEntitiesToRemove <- ZIO.succeed {
                currentVsNew.collect { case (Some(np), None) => np }
            }

            notePersonEntitiesToUpdate <- ZIO.succeed {
                currentVsNew.collect { case (Some(npC), Some(npN)) if npC != npN => npN }
            }

            _ <- run(liftQuery(notePersonEntitiesToAdd).foreach(np => query[NotePersonEntity].insertValue(np)))

            _ <- run {
                liftQuery(notePersonEntitiesToRemove).foreach { npR =>
                    query[NotePersonEntity]
                        .filter(np => np.noteId == npR.noteId && np.personId == npR.personId)
                        .delete
                }
            }

            _ <- run {
                liftQuery(notePersonEntitiesToUpdate).foreach { npU =>
                    query[NotePersonEntity]
                        .filter(np => np.noteId == npU.noteId && np.personId == npU.personId)
                        .updateValue(npU)
                }
            }
        } yield ()
    }
}

package zote.db.repositories

import io.getquill.*
import zio.*
import zote.Ids.{NoteId, PersonId}
import zote.db.QuillContext
import zote.db.model.NotePersonEntity
import zote.db.repositories.includes.given

trait NotePersonRepository {

  def findAllByNoteId(noteId: NoteId): Task[List[NotePersonEntity]]

  def findAllByPersonId(personId: PersonId): Task[List[NotePersonEntity]]

  def insertAll(notePersonEntities: Seq[NotePersonEntity]): Task[Unit]

  def deleteAll(ids: Seq[(NoteId, PersonId)]): Task[Unit]
}

case class NotePersonRepositoryImpl(
    private val quillContext: QuillContext,
) extends NotePersonRepository {

  import quillContext.*

  override def findAllByNoteId(noteId: NoteId): Task[List[NotePersonEntity]] =
    transaction {
      run(query[NotePersonEntity].filter(np => np.noteId == lift(noteId)))
    }

  override def findAllByPersonId(personId: PersonId): Task[List[NotePersonEntity]] =
    transaction {
      run(query[NotePersonEntity].filter(np => np.personId == lift(personId)))
    }

  override def insertAll(notePersonEntities: Seq[NotePersonEntity]): Task[Unit] =
    transaction {
      run(
        liftQuery(notePersonEntities).foreach(np => query[NotePersonEntity].insertValue(np)),
      ).unit
    }

  override def deleteAll(ids: Seq[(NoteId, PersonId)]): Task[Unit] =
    transaction {
      run {
        liftQuery(ids).foreach { case (noteId, personId) =>
          query[NotePersonEntity]
            .filter(np => np.noteId == noteId && np.personId == personId)
            .delete
        }
      }.unit
    }
}

object NotePersonRepositoryImpl {
  lazy val layer = ZLayer.derive[NotePersonRepositoryImpl]
}

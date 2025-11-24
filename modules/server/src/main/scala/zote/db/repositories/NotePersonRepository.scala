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

  def upsert(notePersonEntity: NotePersonEntity): Task[Unit]

  def delete(noteId: NoteId, personId: PersonId): Task[Unit]
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

  override def upsert(notePersonEntity: NotePersonEntity): Task[Unit] = {
    // should be rewritten to onConflictUpdate if on postgres
    transaction {
      for {
        isNew <- run {
          query[NotePersonEntity]
            .filter(np => np.noteId == lift(notePersonEntity.noteId) && np.personId == lift(notePersonEntity.personId))
        }.map(_.isEmpty)

        _ <-
          if (isNew) {
            run(insert(lift(notePersonEntity)))
          } else {
            run(update(lift(notePersonEntity)))
          }
      } yield ()
    }
  }

  inline private def insert = quote { (notePersonEntity: NotePersonEntity) =>
    query[NotePersonEntity].insertValue(notePersonEntity)
  }

  inline private def update = quote { (notePersonEntity: NotePersonEntity) =>
    query[NotePersonEntity]
      .filter(np => np.noteId == notePersonEntity.noteId && np.personId == notePersonEntity.personId)
      .updateValue(notePersonEntity)

  }

  override def delete(noteId: NoteId, personId: PersonId): Task[Unit] =
    transaction {
      run {
        query[NotePersonEntity]
          .filter(np => np.noteId == lift(noteId) && np.personId == lift(personId))
          .delete
      }.unit
    }
}

object NotePersonRepositoryImpl {
  lazy val layer = ZLayer.derive[NotePersonRepositoryImpl]
}

package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NotePersonEntity
import zote.db.repositories.includes.given 

trait NotePersonRepository {

  def findAllByNoteId(noteId: Long): Task[List[NotePersonEntity]]

  def findAllByPersonId(personId: Long): Task[List[NotePersonEntity]]

  def insert(notePersonEntities: Seq[NotePersonEntity]): Task[Unit]

  def delete(notePersonEntities: Seq[NotePersonEntity]): Task[Unit]
}

object NotePersonRepository {
  lazy val layer = ZLayer.derive[NotePersonRepositoryImpl]
}

case class NotePersonRepositoryImpl(
  private val quillContext: QuillContext
) extends NotePersonRepository {

  import quillContext.*

  override def findAllByNoteId(noteId: Long): Task[List[NotePersonEntity]] = transaction {
    run(query[NotePersonEntity].filter(np => np.noteId == lift(noteId)))
  }

  override def findAllByPersonId(personId: Long): Task[List[NotePersonEntity]] = transaction {
    run(query[NotePersonEntity].filter(np => np.personId == lift(personId)))
  }

  override def insert(notePersonEntities: Seq[NotePersonEntity]): Task[Unit] = transaction {
    run(liftQuery(notePersonEntities).foreach(np => query[NotePersonEntity].insertValue(np))).unit
  }

  override def delete(notePersonEntities: Seq[NotePersonEntity]): Task[Unit] = transaction {
    run {
      liftQuery(notePersonEntities).foreach { npD =>
        query[NotePersonEntity]
          .filter(np => np.noteId == npD.noteId && np.personId == npD.personId && np.role == npD.role)
          .delete
      }
    }.unit
  }
}

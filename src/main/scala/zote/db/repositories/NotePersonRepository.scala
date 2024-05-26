package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NotePersonEntity

trait NotePersonRepository {

  def findAllByNoteId(noteId: Long): Task[List[NotePersonEntity]]

  def findAllByPersonId(personId: Long): Task[List[NotePersonEntity]]

  def insert(notePersonEntities: Seq[NotePersonEntity]): Task[Unit]

  def update(notePersonEntities: Seq[NotePersonEntity]): Task[Unit]

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

  override def update(notePersonEntities: Seq[NotePersonEntity]): Task[Unit] = transaction {
    run {
      liftQuery(notePersonEntities).foreach { npU =>
        query[NotePersonEntity]
          .filter(np => np.noteId == npU.noteId && np.personId == npU.personId)
          .updateValue(npU)
      }
    }.unit
  }

  override def delete(notePersonEntities: Seq[NotePersonEntity]): Task[Unit] = transaction {
    run {
      liftQuery(notePersonEntities).foreach { npD =>
        query[NotePersonEntity]
          .filter(np => np.noteId == npD.noteId && np.personId == npD.personId)
          .delete
      }
    }.unit
  }
}

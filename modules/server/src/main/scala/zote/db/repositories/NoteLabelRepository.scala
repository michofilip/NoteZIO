package zote.db.repositories

import io.getquill.*
import zio.*
import zote.Ids.{LabelId, NoteId}
import zote.db.QuillContext
import zote.db.model.NoteLabelEntity
import zote.db.repositories.includes.given

trait NoteLabelRepository {

  def findAllByNoteId(noteId: NoteId): Task[List[NoteLabelEntity]]

  def findAllByLabelId(labelId: LabelId): Task[List[NoteLabelEntity]]

  def insertAll(noteLabelEntities: Seq[NoteLabelEntity]): Task[Unit]

  def deleteAll(ids: Seq[(NoteId, LabelId)]): Task[Unit]
}

case class NoteLabelRepositoryImpl(
    private val quillContext: QuillContext,
) extends NoteLabelRepository {

  import quillContext.*

  override def findAllByNoteId(noteId: NoteId): Task[List[NoteLabelEntity]] =
    transaction {
      run(query[NoteLabelEntity].filter(nl => nl.noteId == lift(noteId)))
    }

  override def findAllByLabelId(labelId: LabelId): Task[List[NoteLabelEntity]] =
    transaction {
      run(query[NoteLabelEntity].filter(nl => nl.labelId == lift(labelId)))
    }

  override def insertAll(noteLabelEntities: Seq[NoteLabelEntity]): Task[Unit] =
    transaction {
      run(
        liftQuery(noteLabelEntities).foreach(nl => query[NoteLabelEntity].insertValue(nl)),
      ).unit
    }

  override def deleteAll(ids: Seq[(NoteId, LabelId)]): Task[Unit] =
    transaction {
      run {
        liftQuery(ids).foreach { case (noteId, labelId) =>
          query[NoteLabelEntity]
            .filter(nl => nl.noteId == noteId && nl.labelId == labelId)
            .delete
        }
      }.unit
    }
}

object NoteLabelRepositoryImpl {
  lazy val layer = ZLayer.derive[NoteLabelRepositoryImpl]
}

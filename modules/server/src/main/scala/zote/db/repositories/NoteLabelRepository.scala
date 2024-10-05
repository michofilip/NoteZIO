package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NoteLabelEntity

trait NoteLabelRepository {

  def findAllByNoteId(noteId: Long): Task[List[NoteLabelEntity]]

  def findAllByLabelId(labelId: Long): Task[List[NoteLabelEntity]]

  def insert(noteLabelEntities: Seq[NoteLabelEntity]): Task[Unit]

  def delete(noteLabelEntities: Seq[NoteLabelEntity]): Task[Unit]
}

case class NoteLabelRepositoryImpl(
    private val quillContext: QuillContext
) extends NoteLabelRepository {

  import quillContext.*

  override def findAllByNoteId(noteId: Long): Task[List[NoteLabelEntity]] =
    transaction {
      run(query[NoteLabelEntity].filter(nl => nl.noteId == lift(noteId)))
    }

  override def findAllByLabelId(labelId: Long): Task[List[NoteLabelEntity]] =
    transaction {
      run(query[NoteLabelEntity].filter(nl => nl.labelId == lift(labelId)))
    }

  override def insert(noteLabelEntities: Seq[NoteLabelEntity]): Task[Unit] =
    transaction {
      run(
        liftQuery(noteLabelEntities).foreach(nl =>
          query[NoteLabelEntity].insertValue(nl)
        )
      ).unit
    }

  override def delete(noteLabelEntities: Seq[NoteLabelEntity]): Task[Unit] =
    transaction {
      run {
        liftQuery(noteLabelEntities).foreach { noteLabelEntity =>
          query[NoteLabelEntity]
            .filter(nl =>
              nl.noteId == noteLabelEntity.noteId && nl.labelId == noteLabelEntity.labelId
            )
            .delete
        }
      }.unit
    }
}

object NoteLabelRepositoryImpl {
  lazy val layer = ZLayer.derive[NoteLabelRepositoryImpl]
}

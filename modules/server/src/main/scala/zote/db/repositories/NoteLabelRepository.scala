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

  def insert(noteLabelEntity: NoteLabelEntity): Task[Unit]

  def delete(noteId: NoteId, labelId: LabelId): Task[Unit]
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

  override def insert(noteLabelEntity: NoteLabelEntity): Task[Unit] =
    transaction {
      run {
        query[NoteLabelEntity].insertValue(lift(noteLabelEntity))
      }.unit
    }

  override def delete(noteId: NoteId, labelId: LabelId): Task[Unit] =
    transaction {
      run {
        query[NoteLabelEntity]
          .filter(nl => nl.noteId == lift(noteId) && nl.labelId == lift(labelId))
          .delete
      }.unit
    }
}

object NoteLabelRepositoryImpl {
  lazy val layer = ZLayer.derive[NoteLabelRepositoryImpl]
}

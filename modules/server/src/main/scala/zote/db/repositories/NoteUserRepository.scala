package zote.db.repositories

import io.getquill.*
import zio.*
import zote.Ids.{NoteId, UserId}
import zote.db.QuillContext
import zote.db.model.NoteUserEntity
import zote.db.repositories.includes.given

trait NoteUserRepository {

  def findAllByNoteId(noteId: NoteId): Task[List[NoteUserEntity]]

  def findAllByUserId(userId: UserId): Task[List[NoteUserEntity]]

  def upsert(noteUserEntity: NoteUserEntity): Task[Unit]

  def delete(noteId: NoteId, userId: UserId): Task[Unit]
}

case class NoteUserRepositoryImpl(
    private val quillContext: QuillContext,
) extends NoteUserRepository {

  import quillContext.*

  override def findAllByNoteId(noteId: NoteId): Task[List[NoteUserEntity]] =
    transaction {
      run(query[NoteUserEntity].filter(np => np.noteId == lift(noteId)))
    }

  override def findAllByUserId(userId: UserId): Task[List[NoteUserEntity]] =
    transaction {
      run(query[NoteUserEntity].filter(np => np.userId == lift(userId)))
    }

  override def upsert(noteUserEntity: NoteUserEntity): Task[Unit] = {
    // should be rewritten to onConflictUpdate if on postgres
    transaction {
      for {
        isNew <- run {
          query[NoteUserEntity]
            .filter(np => np.noteId == lift(noteUserEntity.noteId) && np.userId == lift(noteUserEntity.userId))
        }.map(_.isEmpty)

        _ <-
          if (isNew) {
            run(insert(lift(noteUserEntity)))
          } else {
            run(update(lift(noteUserEntity)))
          }
      } yield ()
    }
  }

  inline private def insert = quote { (noteUserEntity: NoteUserEntity) =>
    query[NoteUserEntity].insertValue(noteUserEntity)
  }

  inline private def update = quote { (noteUserEntity: NoteUserEntity) =>
    query[NoteUserEntity]
      .filter(np => np.noteId == noteUserEntity.noteId && np.userId == noteUserEntity.userId)
      .updateValue(noteUserEntity)

  }

  override def delete(noteId: NoteId, userId: UserId): Task[Unit] =
    transaction {
      run {
        query[NoteUserEntity]
          .filter(np => np.noteId == lift(noteId) && np.userId == lift(userId))
          .delete
      }.unit
    }
}

object NoteUserRepositoryImpl {
  lazy val layer = ZLayer.derive[NoteUserRepositoryImpl]
}

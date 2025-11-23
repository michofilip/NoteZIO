package zote.db.repositories

import io.getquill.*
import zio.*
import zote.Ids.NoteId
import zote.db.QuillContext
import zote.db.model.NoteEntity
import zote.db.repositories.includes.given
import zote.exceptions.NotFoundException

trait NoteRepository {
  def findAll: Task[List[NoteEntity]]

  def findAllByParentNoteId(id: NoteId): Task[List[NoteEntity]]

  def findById(id: NoteId): Task[Option[NoteEntity]]

  final def getById(id: NoteId): Task[NoteEntity] =
    findById(id).someOrFail(NotFoundException(s"Note id: $id not found"))

  def upsert(noteEntity: NoteEntity): Task[NoteEntity]

  def delete(id: NoteId): Task[Unit]
}

case class NoteRepositoryImpl(
    private val quillContext: QuillContext,
) extends NoteRepository {

  import quillContext.*

  override def findAll: Task[List[NoteEntity]] = transaction {
    run(query[NoteEntity])
  }

  override def findAllByParentNoteId(id: NoteId): Task[List[NoteEntity]] =
    transaction {
      run(query[NoteEntity].filter(n => n.parentId.contains(lift(id))))
    }

  override def findById(id: NoteId): Task[Option[NoteEntity]] = transaction {
    run(query[NoteEntity].filter(n => n.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(noteEntity: NoteEntity): Task[NoteEntity] = transaction {
    for {
      id <-
        if (noteEntity.id.isZero) {
          run(insert(lift(noteEntity)))
        } else {
          run(update(lift(noteEntity)))
        }
      note <- getById(id)
    } yield note
  }

  override def delete(id: NoteId): Task[Unit] = transaction {
    run(
      query[NoteEntity]
        .filter(n => n.id == lift(id))
        .delete,
    ).unit
  }

  private inline def insert = quote { (noteEntity: NoteEntity) =>
    query[NoteEntity].insertValue(noteEntity).returning(_.id)
  }

  private inline def update = quote { (noteEntity: NoteEntity) =>
    query[NoteEntity]
      .filter(n => n.id == noteEntity.id)
      .updateValue(noteEntity)
      .returning(_.id)
  }
}

object NoteRepositoryImpl {
  lazy val layer = ZLayer.derive[NoteRepositoryImpl]
}

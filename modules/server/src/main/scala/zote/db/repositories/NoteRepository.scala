package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NoteEntity
import zote.db.repositories.includes.given
import zote.exceptions.NotFoundException

trait NoteRepository {
  def findAll: Task[List[NoteEntity]]

  def findAllByParentId(parentId: Long): Task[List[NoteEntity]]

  def findById(id: Long): Task[Option[NoteEntity]]

  final def getById(id: Long): Task[NoteEntity] =
    findById(id).someOrFail(NotFoundException(s"Note id: $id not found"))

  def upsert(noteEntity: NoteEntity): Task[NoteEntity]

  def delete(id: Long): Task[Unit]
}

object NoteRepository {
  lazy val layer = ZLayer.derive[NoteRepositoryImpl]
}

case class NoteRepositoryImpl(
    private val quillContext: QuillContext
) extends NoteRepository {

  import quillContext.*

  override def findAll: Task[List[NoteEntity]] = transaction {
    run(query[NoteEntity])
  }

  override def findAllByParentId(parentId: Long): Task[List[NoteEntity]] =
    transaction {
      run(query[NoteEntity].filter(n => n.parentId.contains(lift(parentId))))
    }

  override def findById(id: Long): Task[Option[NoteEntity]] = transaction {
    run(query[NoteEntity].filter(n => n.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(noteEntity: NoteEntity): Task[NoteEntity] = transaction {
    for {
      id <-
        if (noteEntity.id == 0) {
          run(insert(lift(noteEntity)))
        } else {
          run(update(lift(noteEntity)))
        }
      note <- getById(id)
    } yield note
  }

  override def delete(id: Long): Task[Unit] = transaction {
    run(
      query[NoteEntity]
        .filter(i => i.id == lift(id))
        .delete
    ).unit
  }

  private inline def insert = quote { (noteEntity: NoteEntity) =>
    query[NoteEntity].insertValue(noteEntity).returning(_.id)
  }

  private inline def update = quote { (noteEntity: NoteEntity) =>
    query[NoteEntity]
      .filter(i => i.id == noteEntity.id)
      .updateValue(noteEntity)
      .returning(_.id)
  }
}

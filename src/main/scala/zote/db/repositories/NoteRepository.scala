package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.NoteEntity
import zote.exceptions.NotFoundException

trait NoteRepository {
  def findAll: Task[List[NoteEntity]]

  def findAllByParentId(parentId: Long): Task[List[NoteEntity]]

  def findById(id: Long): Task[Option[NoteEntity]]

  final def getById(id: Long): Task[NoteEntity] = findById(id).someOrFail(NotFoundException(s"Note id: $id not found"))

  def upsert(noteEntity: NoteEntity): Task[NoteEntity]

  def delete(id: Long): Task[NoteEntity]
}

object NoteRepository {
  lazy val layer = ZLayer.derive[NoteRepositoryImpl]
}

case class NoteRepositoryImpl(
  private val quillContext: QuillContext
) extends NoteRepository {

  import quillContext.postgres.*

  override def findAll: Task[List[NoteEntity]] = {
    run(query[NoteEntity])
  }

  override def findAllByParentId(parentId: Long): Task[List[NoteEntity]] = {
    run(query[NoteEntity].filter(n => n.parentId.contains(lift(parentId))))
  }


  override def findById(id: Long): Task[Option[NoteEntity]] = {
    run(query[NoteEntity].filter(n => n.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(noteEntity: NoteEntity): Task[NoteEntity] = {
    if (noteEntity.id == 0) {
      run(insert(lift(noteEntity)))
    } else {
      run(update(lift(noteEntity)))
    }
  }

  override def delete(id: Long): Task[NoteEntity] = {
    run(query[NoteEntity].filter(i => i.id == lift(id)).delete.returning(n => n))
  }

  private inline def insert = quote { (noteEntity: NoteEntity) =>
    query[NoteEntity].insertValue(noteEntity).returning(n => n)
  }

  private inline def update = quote { (noteEntity: NoteEntity) =>
    query[NoteEntity].filter(i => i.id == noteEntity.id).updateValue(noteEntity).returning(n => n)
  }
}

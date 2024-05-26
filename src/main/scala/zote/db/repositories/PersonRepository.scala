package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.exceptions.NotFoundException
import zote.utils.Utils

trait PersonRepository {
  def findAll: Task[List[PersonEntity]]

  def findById(id: Long): Task[Option[PersonEntity]]

  final def getById(id: Long): Task[PersonEntity] = findById(id).someOrFail(NotFoundException(s"Person id: $id not found"))

  def upsert(personEntity: PersonEntity): Task[PersonEntity]

  def delete(id: Long): Task[PersonEntity]
}

object PersonRepository {
  lazy val layer = ZLayer.derive[PersonRepositoryImpl]
}

case class PersonRepositoryImpl(
  private val quillContext: QuillContext
) extends PersonRepository {

  import quillContext.*

  override def findAll: Task[List[PersonEntity]] = {
    run(query[PersonEntity])
  }

  override def findById(id: Long): Task[Option[PersonEntity]] = {
    run(query[PersonEntity].filter(p => p.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(personEntity: PersonEntity): Task[PersonEntity] = {
    if (personEntity.id == 0) {
      run(insert(lift(personEntity)))
    } else {
      run(update(lift(personEntity)))
    }
  }

  override def delete(id: Long): Task[PersonEntity] = {
    run(query[PersonEntity].filter(p => p.id == lift(id)).delete.returning(Utils.identity))
  }

  private inline def insert = quote { (personEntity: PersonEntity) =>
    query[PersonEntity].insertValue(personEntity).returning(Utils.identity)
  }

  private inline def update = quote { (personEntity: PersonEntity) =>
    query[PersonEntity].filter(p => p.id == personEntity.id).updateValue(personEntity).returning(Utils.identity)
  }
}

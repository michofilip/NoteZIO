package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.exceptions.NotFoundException

trait PersonRepository {
  def findAll: Task[List[PersonEntity]]

  def findById(id: Long): Task[Option[PersonEntity]]

  final def getById(id: Long): Task[PersonEntity] =
    findById(id).someOrFail(NotFoundException(s"Person id: $id not found"))

  def upsert(personEntity: PersonEntity): Task[PersonEntity]

  def delete(id: Long): Task[Unit]
}

case class PersonRepositoryImpl(
    private val quillContext: QuillContext
) extends PersonRepository {

  import quillContext.*

  override def findAll: Task[List[PersonEntity]] = transaction {
    run(query[PersonEntity])
  }

  override def findById(id: Long): Task[Option[PersonEntity]] = transaction {
    run(query[PersonEntity].filter(p => p.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(personEntity: PersonEntity): Task[PersonEntity] =
    transaction {
      for {
        id <-
          if (personEntity.id == 0) {
            run(insert(lift(personEntity)))
          } else {
            run(update(lift(personEntity)))
          }
        person <- getById(id)
      } yield person
    }

  override def delete(id: Long): Task[Unit] = transaction {
    run {
      query[PersonEntity]
        .filter(p => p.id == lift(id))
        .delete
    }.unit
  }

  private inline def insert = quote { (personEntity: PersonEntity) =>
    query[PersonEntity].insertValue(personEntity).returning(_.id)
  }

  private inline def update = quote { (personEntity: PersonEntity) =>
    query[PersonEntity]
      .filter(p => p.id == personEntity.id)
      .updateValue(personEntity)
      .returning(_.id)
  }
}

object PersonRepositoryImpl {
  lazy val layer = ZLayer.derive[PersonRepositoryImpl]
}

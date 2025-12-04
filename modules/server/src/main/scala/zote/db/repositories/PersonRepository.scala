package zote.db.repositories

import io.getquill.*
import zio.*
import zote.Ids.PersonId
import zote.db.QuillContext
import zote.db.model.PersonEntity
import zote.db.repositories.includes.given
import zote.exceptions.NotFoundException

trait PersonRepository {
  def findAll: Task[List[PersonEntity]]

  def findById(id: PersonId): Task[Option[PersonEntity]]

  final def getById(id: PersonId): Task[PersonEntity] =
    findById(id).someOrFail(NotFoundException(s"Person id: ${id.value} not found"))

  def findByName(name: String): Task[Option[PersonEntity]]

  def upsert(personEntity: PersonEntity): Task[PersonEntity]

  def delete(id: PersonId): Task[Unit]
}

case class PersonRepositoryImpl(
    private val quillContext: QuillContext,
) extends PersonRepository {

  import quillContext.*

  override def findAll: Task[List[PersonEntity]] = transaction {
    run(query[PersonEntity])
  }

  override def findById(id: PersonId): Task[Option[PersonEntity]] = transaction {
    run(query[PersonEntity].filter(p => p.id == lift(id)))
      .map(_.headOption)
  }

  override def findByName(name: String): Task[Option[PersonEntity]] = transaction {
    run(query[PersonEntity].filter(p => p.name == lift(name)))
      .map(_.headOption)
  }

  override def upsert(personEntity: PersonEntity): Task[PersonEntity] =
    transaction {
      for {
        id <-
          if (personEntity.id.isZero) {
            run(insert(lift(personEntity)))
          } else {
            run(update(lift(personEntity)))
          }
        person <- getById(id)
      } yield person
    }

  override def delete(id: PersonId): Task[Unit] = transaction {
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

package zote.db.repositories

import io.getquill.*
import zio.*
import zote.Ids.UserId
import zote.db.QuillContext
import zote.db.model.UserEntity
import zote.db.repositories.includes.given
import zote.exceptions.NotFoundException

trait UserRepository {
  def findAll: Task[List[UserEntity]]

  def findById(id: UserId): Task[Option[UserEntity]]

  final def getById(id: UserId): Task[UserEntity] =
    findById(id).someOrFail(NotFoundException(s"User id: ${id.value} not found"))

  def findByName(name: String): Task[Option[UserEntity]]

  def upsert(userEntity: UserEntity): Task[UserEntity]

  def delete(id: UserId): Task[Unit]
}

case class UserRepositoryImpl(
    private val quillContext: QuillContext,
) extends UserRepository {

  import quillContext.*

  override def findAll: Task[List[UserEntity]] = transaction {
    run(query[UserEntity])
  }

  override def findById(id: UserId): Task[Option[UserEntity]] = transaction {
    run(query[UserEntity].filter(p => p.id == lift(id)))
      .map(_.headOption)
  }

  override def findByName(name: String): Task[Option[UserEntity]] = transaction {
    run(query[UserEntity].filter(p => p.name == lift(name)))
      .map(_.headOption)
  }

  override def upsert(userEntity: UserEntity): Task[UserEntity] =
    transaction {
      for {
        id <-
          if (userEntity.id.isZero) {
            run(insert(lift(userEntity)))
          } else {
            run(update(lift(userEntity)))
          }
        user <- getById(id)
      } yield user
    }

  override def delete(id: UserId): Task[Unit] = transaction {
    run {
      query[UserEntity]
        .filter(p => p.id == lift(id))
        .delete
    }.unit
  }

  private inline def insert = quote { (userEntity: UserEntity) =>
    query[UserEntity].insertValue(userEntity).returning(_.id)
  }

  private inline def update = quote { (userEntity: UserEntity) =>
    query[UserEntity]
      .filter(p => p.id == userEntity.id)
      .updateValue(userEntity)
      .returning(_.id)
  }
}

object UserRepositoryImpl {
  lazy val layer = ZLayer.derive[UserRepositoryImpl]
}

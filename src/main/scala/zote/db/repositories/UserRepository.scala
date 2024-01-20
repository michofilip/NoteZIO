package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.UserEntity

trait UserRepository {
    def findAll: Task[Seq[UserEntity]]

    def findById(id: Long): Task[Option[UserEntity]]

    def save(userEntity: UserEntity): Task[UserEntity]

    def delete(id: Long): Task[UserEntity]

    def findExistingIds(ids: Set[Long]): Task[List[Long]]
}

object UserRepository {
    lazy val layer = ZLayer.derive[UserRepositoryImpl]
}

case class UserRepositoryImpl(
    private val quillContext: QuillContext
) extends UserRepository {

    import quillContext.postgres.*

    override def findAll: Task[Seq[UserEntity]] = {
        run(query[UserEntity])
    }

    override def findById(id: Long): Task[Option[UserEntity]] = {
        run(query[UserEntity].filter(u => u.id == lift(id)))
            .map(_.headOption)
    }

    override def save(userEntity: UserEntity): Task[UserEntity] = {
        if (userEntity.id == 0) {
            run(insertNote(lift(userEntity)))
        } else {
            run(updateNote(lift(userEntity)))
        }
    }

    override def delete(id: Long): Task[UserEntity] = {
        run(query[UserEntity].filter(u => u.id == lift(id)).delete.returning(u => u))
    }

    override def findExistingIds(ids: Set[Long]): Task[List[Long]] = {
        run(query[UserEntity].filter(u => liftQuery(ids).contains(u.id)).map(_.id))
    }

    private inline def insertNote = quote { (userEntity: UserEntity) =>
        query[UserEntity].insertValue(userEntity).returning(u => u)
    }

    private inline def updateNote = quote { (userEntity: UserEntity) =>
        query[UserEntity].filter(u => u.id == userEntity.id).updateValue(userEntity).returning(u => u)
    }
}

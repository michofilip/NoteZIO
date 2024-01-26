package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.PersonEntity

trait PersonRepository {
    def findAll: Task[Seq[PersonEntity]]

    def findById(id: Long): Task[Option[PersonEntity]]

    def upsert(userEntity: PersonEntity): Task[PersonEntity]

    def delete(id: Long): Task[PersonEntity]

    def findExistingIds(ids: Set[Long]): Task[List[Long]]
}

object PersonRepository {
    lazy val layer = ZLayer.derive[PersonRepositoryImpl]
}

case class PersonRepositoryImpl(
    private val quillContext: QuillContext
) extends PersonRepository {

    import quillContext.postgres.*

    override def findAll: Task[Seq[PersonEntity]] = {
        run(query[PersonEntity])
    }

    override def findById(id: Long): Task[Option[PersonEntity]] = {
        run(query[PersonEntity].filter(u => u.id == lift(id)))
            .map(_.headOption)
    }

    override def upsert(userEntity: PersonEntity): Task[PersonEntity] = {
        if (userEntity.id == 0) {
            run(insert(lift(userEntity)))
        } else {
            run(update(lift(userEntity)))
        }
    }

    override def delete(id: Long): Task[PersonEntity] = {
        run(query[PersonEntity].filter(u => u.id == lift(id)).delete.returning(u => u))
    }

    override def findExistingIds(ids: Set[Long]): Task[List[Long]] = {
        run(query[PersonEntity].filter(u => liftQuery(ids).contains(u.id)).map(_.id))
    }

    private inline def insert = quote { (userEntity: PersonEntity) =>
        query[PersonEntity].insertValue(userEntity).returning(u => u)
    }

    private inline def update = quote { (userEntity: PersonEntity) =>
        query[PersonEntity].filter(u => u.id == userEntity.id).updateValue(userEntity).returning(u => u)
    }
}

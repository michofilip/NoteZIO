package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.LabelEntity
import zote.exceptions.NotFoundException

trait LabelRepository {
  def findAll: Task[List[LabelEntity]]

  def findById(id: Long): Task[Option[LabelEntity]]

  final def getById(id: Long): Task[LabelEntity] =
    findById(id).someOrFail(NotFoundException(s"Label id: $id not found"))

  def upsert(labelEntity: LabelEntity): Task[LabelEntity]

  def delete(id: Long): Task[Unit]
}

case class LabelRepositoryImpl(
    private val quillContext: QuillContext
) extends LabelRepository {

  import quillContext.*

  override def findAll: Task[List[LabelEntity]] = transaction {
    run(query[LabelEntity])
  }

  override def findById(id: Long): Task[Option[LabelEntity]] = transaction {
    run(query[LabelEntity].filter(l => l.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(labelEntity: LabelEntity): Task[LabelEntity] =
    transaction {
      for {
        id <-
          if (labelEntity.id == 0) {
            run(insert(lift(labelEntity)))
          } else {
            run(update(lift(labelEntity)))
          }
        label <- getById(id)
      } yield label
    }

  override def delete(id: Long): Task[Unit] = transaction {
    run(
      query[LabelEntity]
        .filter(l => l.id == lift(id))
        .delete
    ).unit
  }

  private inline def insert = quote { (labelEntity: LabelEntity) =>
    query[LabelEntity].insertValue(labelEntity).returning(_.id)
  }

  private inline def update = quote { (labelEntity: LabelEntity) =>
    query[LabelEntity]
      .filter(l => l.id == labelEntity.id)
      .updateValue(labelEntity)
      .returning(_.id)
  }
}

object LabelRepositoryImpl {
  lazy val layer = ZLayer.derive[LabelRepositoryImpl]
}

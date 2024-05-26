package zote.db.repositories

import io.getquill.*
import zio.*
import zote.db.QuillContext
import zote.db.model.LabelEntity
import zote.exceptions.NotFoundException
import zote.utils.Utils

trait LabelRepository {
  def findAll: Task[List[LabelEntity]]

  def findById(id: Long): Task[Option[LabelEntity]]

  final def getById(id: Long): Task[LabelEntity] =
    findById(id).someOrFail(NotFoundException(s"Label id: $id not found"))

  def upsert(labelEntity: LabelEntity): Task[LabelEntity]

  def delete(id: Long): Task[LabelEntity]
}

object LabelRepository {
  lazy val layer = ZLayer.derive[LabelRepositoryImpl]
}

case class LabelRepositoryImpl(
    private val quillContext: QuillContext
) extends LabelRepository {

  import quillContext.*

  override def findAll: Task[List[LabelEntity]] = {
    run(query[LabelEntity])
  }

  override def findById(id: Long): Task[Option[LabelEntity]] = {
    run(query[LabelEntity].filter(l => l.id == lift(id)))
      .map(_.headOption)
  }

  override def upsert(labelEntity: LabelEntity): Task[LabelEntity] = {
    if (labelEntity.id == 0) {
      run(insert(lift(labelEntity)))
    } else {
      run(update(lift(labelEntity)))
    }
  }

  override def delete(id: Long): Task[LabelEntity] = {
    run(
      query[LabelEntity]
        .filter(l => l.id == lift(id))
        .delete
        .returning(Utils.identity)
    )
  }

  private inline def insert = quote { (labelEntity: LabelEntity) =>
    query[LabelEntity].insertValue(labelEntity).returning(Utils.identity)
  }

  private inline def update = quote { (labelEntity: LabelEntity) =>
    query[LabelEntity]
      .filter(l => l.id == labelEntity.id)
      .updateValue(labelEntity)
      .returning(Utils.identity)
  }
}

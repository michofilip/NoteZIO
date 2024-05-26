package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.db.QuillContext
import zote.db.model.LabelEntity
import zote.db.repositories.{LabelRepository, NoteLabelRepository}
import zote.dto.{Label, LabelForm}
import zote.exceptions.CannotDeleteException

trait LabelService {
  def getAll: Task[List[Label]]

  def getById(id: Long): Task[Label]

  def create(labelForm: LabelForm): Task[Label]

  def update(id: Long, labelForm: LabelForm): Task[Label]

  def delete(id: Long, force: Boolean): Task[Unit]
}

object LabelService {
  lazy val layer = ZLayer.derive[LabelServiceImpl]
}

case class LabelServiceImpl(
  private val labelRepository: LabelRepository,
  private val noteLabelRepository: NoteLabelRepository,
  private val quillContext: QuillContext
) extends LabelService {

  import quillContext.*

  override def getAll: Task[List[Label]] = transaction {
    labelRepository.findAll.flatMap { labelEntities =>
      ZIO.foreachPar(labelEntities)(toLabel)
    }
  }

  override def getById(id: Long): Task[Label] = transaction {
    labelRepository.getById(id).flatMap(toLabel)
  }

  override def create(labelForm: LabelForm): Task[Label] = transaction {
    for {
      _ <- LabelForm.validateZIO(labelForm)
      labelEntity <- labelRepository.upsert {
        LabelEntity(name = labelForm.name)
      }
      label <- toLabel(labelEntity)
    } yield label
  }

  override def update(id: Long, labelForm: LabelForm): Task[Label] = transaction {
    for {
      _ <- LabelForm.validateZIO(labelForm)
      labelEntity <- labelRepository.getById(id)
      labelEntity <- labelRepository.upsert {
        labelEntity.modify(_.name).setTo(labelForm.name)
      }
      label <- toLabel(labelEntity)
    } yield label
  }

  override def delete(id: Long, force: Boolean): Task[Unit] = transaction {
    for {
      _ <- labelRepository.getById(id)
      noteLabelEntities <- noteLabelRepository.findAllByLabelId(id)
      _ <- ZIO.unless(noteLabelEntities.isEmpty) {
        if (force) {
          noteLabelRepository.delete(noteLabelEntities)
        } else {
          ZIO.fail(CannotDeleteException(s"Label id: $id can not be deleted"))
        }
      }
      _ <- labelRepository.delete(id)
    } yield ()
  }

  private def toLabel(labelEntity: LabelEntity) = {
    ZIO.succeed {
      Label(
        id = labelEntity.id,
        name = labelEntity.name,
      )
    }
  }
}

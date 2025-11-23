package zote.services

import com.softwaremill.quicklens.*
import zio.*
import zote.Ids.LabelId
import zote.db.QuillContext
import zote.db.model.LabelEntity
import zote.db.repositories.{LabelRepository, NoteLabelRepository}
import zote.dto.Label
import zote.dto.form.LabelForm
import zote.dto.validation.Validator

trait LabelService {
  def getAll: Task[List[Label]]

  def getById(id: LabelId): Task[Label]

  def create(labelForm: LabelForm): Task[Label]

  def update(id: LabelId, labelForm: LabelForm): Task[Label]

  def delete(id: LabelId): Task[Unit]
}

case class LabelServiceImpl(
    private val labelRepository: LabelRepository,
    private val noteLabelRepository: NoteLabelRepository,
    private val quillContext: QuillContext,
) extends LabelService {

  import quillContext.*

  override def getAll: Task[List[Label]] = transaction {
    labelRepository.findAll.flatMap { labelEntities =>
      ZIO.foreachPar(labelEntities)(toLabel)
    }
  }

  override def getById(id: LabelId): Task[Label] = transaction {
    labelRepository.getById(id).flatMap(toLabel)
  }

  override def create(labelForm: LabelForm): Task[Label] = transaction {
    for {
      _ <- Validator.validateZIO(labelForm)
      labelEntity <- labelRepository.upsert {
        LabelEntity(name = labelForm.name)
      }
      label <- toLabel(labelEntity)
    } yield label
  }

  override def update(id: LabelId, labelForm: LabelForm): Task[Label] =
    transaction {
      for {
        _           <- Validator.validateZIO(labelForm)
        labelEntity <- labelRepository.getById(id)
        labelEntity <- labelRepository.upsert {
          labelEntity.modify(_.name).setTo(labelForm.name)
        }
        label <- toLabel(labelEntity)
      } yield label
    }

  override def delete(id: LabelId): Task[Unit] = transaction {
    for {
      _                 <- labelRepository.getById(id)
      noteLabelEntities <- noteLabelRepository.findAllByLabelId(id)
      _ <- noteLabelRepository
        .deleteAll(noteLabelEntities.map(entity => (entity.noteId, entity.labelId)))
        .unless(noteLabelEntities.isEmpty)
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

object LabelServiceImpl {
  lazy val layer = ZLayer.derive[LabelServiceImpl]
}

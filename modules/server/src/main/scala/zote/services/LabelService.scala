package zote.services

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
      _           <- Validator.validateZIO(labelForm)
      labelEntity <- toLabelEntity(labelForm)
      labelEntity <- labelRepository.upsert(labelEntity)
      label       <- toLabel(labelEntity)
    } yield label
  }

  override def update(id: LabelId, labelForm: LabelForm): Task[Label] = {
    transaction {
      for {
        _           <- Validator.validateZIO(labelForm)
        labelEntity <- labelRepository.getById(id)
        labelEntity <- toLabelEntity(labelForm, labelEntity)
        labelEntity <- labelRepository.upsert(labelEntity)
        label       <- toLabel(labelEntity)
      } yield label
    }
  }

  inline private def toLabelEntity(
      labelForm: LabelForm,
      inline labelEntity: LabelEntity | Unit = (),
  ): Task[LabelEntity] = {
    inline labelEntity match {
      case labelEntity: LabelEntity => ZIO.succeed(labelEntity.copy(name = labelForm.name))
      case _                        => ZIO.succeed(LabelEntity(name = labelForm.name))
    }
  }

  override def delete(id: LabelId): Task[Unit] = transaction {
    for {
      _                 <- labelRepository.getById(id)
      noteLabelEntities <- noteLabelRepository.findAllByLabelId(id)
      _ <- ZIO.foreachDiscard(noteLabelEntities) { noteLabelEntity =>
        noteLabelRepository.delete(noteLabelEntity.noteId, noteLabelEntity.labelId)
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

object LabelServiceImpl {
  lazy val layer = ZLayer.derive[LabelServiceImpl]
}

package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.Ids.LabelId
import zote.dto.response.EmptyResponse.given
import zote.dto.response.LabelResponse.given
import zote.dto.response.LabelsResponse.given
import zote.dto.response.Response
import zote.endpoints.LabelEndpoints
import zote.services.LabelService
import zote.services.validation.LabelValidationService

class LabelController(
    private val labelService: LabelService,
    private val labelValidationService: LabelValidationService,
) extends Controller
    with LabelEndpoints {

  private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
    labelService.getAll.map(labels => Response.success(labels))
  }

  private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
    labelService.getById(LabelId(id)).map(label => Response.success(label))
  }

  private val create = createEndpoint.zServerLogic[Any] { labelForm =>
    for {
      labelForm <- labelValidationService.validate(labelForm)
      label     <- labelService.create(labelForm)
    } yield Response.success(label)
  }

  private val update = updateEndpoint.zServerLogic[Any] { (id, labelForm) =>
    for {
      id        <- ZIO.succeed(LabelId(id))
      labelForm <- labelValidationService.validate(labelForm)
      label     <- labelService.update(id, labelForm)
    } yield Response.success(label)
  }

  private val delete = deleteEndpoint.zServerLogic[Any] { id =>
    labelService.delete(LabelId(id)).as(Response.success)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create,
    update,
    delete,
  )
}

object LabelController {
  lazy val layer = ZLayer.derive[LabelController]
}

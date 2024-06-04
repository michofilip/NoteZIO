package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.endpoints.LabelEndpoints
import zote.services.LabelService

class LabelController(
    private val labelService: LabelService
) extends Controller
    with LabelEndpoints {

  private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
    labelService.getAll
  }

  private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
    labelService.getById(id)
  }

  private val create = createEndpoint.zServerLogic[Any] { labelForm =>
    labelService.create(labelForm)
  }

  private val update = updateEndpoint.zServerLogic[Any] { (id, labelForm) =>
    labelService.update(id, labelForm)
  }

  private val delete = deleteEndpoint.zServerLogic[Any] { (id, force) =>
    labelService.delete(id, force.getOrElse(false))
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create,
    update,
    delete
  )
}

object LabelController {
  lazy val layer = ZLayer.derive[LabelController]
}

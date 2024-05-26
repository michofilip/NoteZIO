package zote.controllers

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import sttp.tapir.{AnyEndpoint, path}
import zio.*
import zote.dto.{Label, LabelForm, Person, PersonForm}
import zote.services.{LabelService, PersonService}

class LabelController(
  private val labelService: LabelService,
) extends Controller {
  override protected val tag: String = "label"

  private val getAllEndpoint = baseEndpoint
    .name("getAll")
    .description("getAll")
    .get
    .in("label")
    .out(jsonBody[List[Label]])

  private val getByIdEndpoint = baseEndpoint
    .name("getById")
    .description("getById")
    .get
    .in("label" / path[Long]("id"))
    .out(jsonBody[Label])

  private val createEndpoint = baseEndpoint
    .name("create")
    .description("create")
    .post
    .in("label")
    .in(jsonBody[LabelForm])
    .out(jsonBody[Label])

  private val updateEndpoint = baseEndpoint
    .name("update")
    .description("update")
    .put
    .in("label" / path[Long]("id"))
    .in(jsonBody[LabelForm])
    .out(jsonBody[Label])

  private val deleteEndpoint = baseEndpoint
    .name("delete")
    .description("delete")
    .delete
    .in("label" / path[Long]("id"))
    .in(query[Option[Boolean]]("force"))
    .out(emptyOutput)

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

  override val endpoints: List[AnyEndpoint] = List(
    getAllEndpoint,
    getByIdEndpoint,
    createEndpoint,
    updateEndpoint,
    deleteEndpoint
  )
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

package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.endpoints.PersonEndpoints
import zote.services.PersonService

class PersonController(
    private val personService: PersonService
) extends Controller
    with PersonEndpoints {

  private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
    personService.getAll
  }

  private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
    personService.getById(id)
  }

  private val create = createEndpoint.zServerLogic[Any] { personsForm =>
    personService.create(personsForm)
  }

  private val update = updateEndpoint.zServerLogic[Any] { (id, personsForm) =>
    personService.update(id, personsForm)
  }

  private val delete = deleteEndpoint.zServerLogic[Any] { (id, force) =>
    personService.delete(id, force.getOrElse(false))
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create,
    update,
    delete
  )
}

object PersonController {
  lazy val layer = ZLayer.derive[PersonController]
}

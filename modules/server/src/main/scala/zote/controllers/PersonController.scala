package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.Ids.PersonId
import zote.dto.response.{PersonResponse, PersonsResponse, Response}
import zote.endpoints.PersonEndpoints
import zote.services.PersonService
import zote.services.validation.PersonValidationService

class PersonController(
    private val personService: PersonService,
    private val personValidationService: PersonValidationService,
) extends Controller
    with PersonEndpoints {

  private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
    personService.getAll.map(persons => Response.success(persons))
  }

  private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
    personService.getById(PersonId(id)).map(person => Response.success(person))
  }

  private val create = createEndpoint.zServerLogic[Any] { personForm =>
    for {
      personForm <- personValidationService.validateForCreate(personForm)
      person     <- personService.create(personForm)
    } yield Response.success(person)

  }

  private val update = updateEndpoint.zServerLogic[Any] { (id, personForm) =>
    for {
      id         <- ZIO.succeed(PersonId(id))
      personForm <- personValidationService.validateForUpdate(id, personForm)
      person     <- personService.update(id, personForm)
    } yield Response.success(person)
  }

  private val delete = deleteEndpoint.zServerLogic[Any] { id =>
    personService.delete(PersonId(id)).as(Response.success)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create,
    update,
    delete,
  )
}

object PersonController {
  lazy val layer = ZLayer.derive[PersonController]
}

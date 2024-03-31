package zote.controllers

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import sttp.tapir.{AnyEndpoint, path}
import zio.*
import zote.dto.{Person, PersonForm}
import zote.services.PersonService

class PersonController(
    private val personService: PersonService
) extends Controller {
    override protected val tag: String = "persons"

    private val getAllEndpoint = baseEndpoint
        .name("getAll")
        .description("getAll")
        .get
        .in("persons")
        .out(jsonBody[Seq[Person]])

    private val getByIdEndpoint = baseEndpoint
        .name("getById")
        .description("getById")
        .get
        .in("persons" / path[Long]("id"))
        .out(jsonBody[Person])

    private val createEndpoint = baseEndpoint
        .name("create")
        .description("create")
        .post
        .in("persons")
        .in(jsonBody[PersonForm])
        .out(jsonBody[Person])

    private val updateEndpoint = baseEndpoint
        .name("update")
        .description("update")
        .put
        .in("persons" / path[Long]("id"))
        .in(jsonBody[PersonForm])
        .out(jsonBody[Person])

    private val deleteEndpoint = baseEndpoint
        .name("delete")
        .description("delete")
        .delete
        .in("persons" / path[Long]("id"))
        .out(emptyOutput)

    private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
        personService.getAll
    }

    private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
        personService.getById(id)
    }

    private val create = createEndpoint.zServerLogic[Any] { personsForm =>
        personService.create(personsForm)
    }

    private val update = updateEndpoint.zServerLogic[Any] { case (id, personsForm) =>
        personService.update(id, personsForm)
    }

    private val delete = deleteEndpoint.zServerLogic[Any] { id =>
        personService.delete(id)
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

object PersonController {
    lazy val layer = ZLayer.derive[PersonController]
}

package zote.controllers

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import sttp.tapir.{AnyEndpoint, path}
import zio.*
import zote.dto.{User, UserForm}
import zote.services.UserService

class UserController(
    private val userService: UserService
) extends Controller {

    private val userEndpoint = endpoint.tag("users")

    private val getAllEndpoint = userEndpoint
        .name("getAll")
        .description("getAll")
        .get
        .in("users")
        .out(jsonBody[Seq[User]])
        .errorOut(getErrorOut)

    private val getByIdEndpoint = userEndpoint
        .name("getById")
        .description("getById")
        .get
        .in("users" / path[Long]("id"))
        .out(jsonBody[User])
        .errorOut(getErrorOut)

    private val createEndpoint = userEndpoint
        .name("create")
        .description("create")
        .post
        .in("users")
        .in(jsonBody[UserForm])
        .out(jsonBody[User])
        .errorOut(getErrorOut)

    private val updateEndpoint = userEndpoint
        .name("update")
        .description("update")
        .put
        .in("users" / path[Long]("id"))
        .in(jsonBody[UserForm])
        .out(jsonBody[User])
        .errorOut(getErrorOut)

    private val deleteEndpoint = userEndpoint
        .name("delete")
        .description("delete")
        .delete
        .in("users" / path[Long]("id"))
        .out(emptyOutput)
        .errorOut(getErrorOut)

    private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
        userService.getAll
    }

    private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
        userService.getById(id)
    }

    private val create = createEndpoint.zServerLogic[Any] { userForm =>
        userService.create(userForm)
    }

    private val update = updateEndpoint.zServerLogic[Any] { case (id, userForm) =>
        userService.update(id, userForm)
    }

    private val delete = deleteEndpoint.zServerLogic[Any] { id =>
        userService.delete(id)
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

object UserController {
    lazy val layer = ZLayer.derive[UserController]
}

package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.Ids.UserId
import zote.dto.response.EmptyResponse.given
import zote.dto.response.UserResponse.given
import zote.dto.response.UsersResponse.given
import zote.dto.response.Response
import zote.endpoints.UserEndpoints
import zote.services.UserService
import zote.services.validation.UserValidationService

case class UserController(
    private val userService: UserService,
    private val userValidationService: UserValidationService,
) extends Controller
    with UserEndpoints {

  private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
    userService.getAll.map(users => Response.success(users))
  }

  private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
    userService.getById(UserId(id)).map(user => Response.success(user))
  }

  private val create = createEndpoint.zServerLogic[Any] { userForm =>
    for {
      userForm <- userValidationService.validateForCreate(userForm)
      user     <- userService.create(userForm)
    } yield Response.success(user)

  }

  private val update = updateEndpoint.zServerLogic[Any] { (id, userForm) =>
    for {
      id       <- ZIO.succeed(UserId(id))
      userForm <- userValidationService.validateForUpdate(id, userForm)
      user     <- userService.update(id, userForm)
    } yield Response.success(user)
  }

  private val delete = deleteEndpoint.zServerLogic[Any] { id =>
    userService.delete(UserId(id)).as(Response.success)
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create,
    update,
    delete,
  )
}

object UserController {
  lazy val layer = ZLayer.derive[UserController]
}

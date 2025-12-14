package zote.endpoints

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import zio.*
import zote.dto.User
import zote.dto.form.UserForm
import zote.dto.response.{EmptyResponse, UserResponse, UsersResponse}

trait UserEndpoints extends Endpoints {
  override protected val tag: String = "users"

  val getAllEndpoint =
    jsonEndpoint[List[User], UsersResponse]
      .description("Returns a list of all users. [GET] /api/v1/users")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[User, UserResponse]
      .description("Returns details of a user by its identifier. [GET] /api/v1/users/{id}")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[User, UserResponse]
      .description("Creates a new user and returns the created object. [POST] /api/v1/users")
      .post
      .in(tag)
      .in(jsonBody[UserForm.Raw])

  val updateEndpoint =
    jsonEndpoint[User, UserResponse]
      .description("Updates a user by its identifier and returns the updated object. [PUT] /api/v1/users/{id}")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[UserForm.Raw])

  val deleteEndpoint =
    jsonEndpoint[Nothing, EmptyResponse]
      .description("Deletes a user by its identifier. [DELETE] /api/v1/users/{id}")
      .delete
      .in(tag / path[Long]("id"))

  final override val endpoints: List[AnyEndpoint] = List(
    getAllEndpoint,
    getByIdEndpoint,
    createEndpoint,
    updateEndpoint,
    deleteEndpoint,
  )
}

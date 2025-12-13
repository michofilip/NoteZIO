package zote.endpoints

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import zio.*
import zote.dto.Person
import zote.dto.form.PersonForm
import zote.dto.response.{EmptyResponse, PersonResponse, PersonsResponse}

trait PersonEndpoints extends Endpoints {
  override protected val tag: String = "persons"

  val getAllEndpoint =
    jsonEndpoint[List[Person], PersonsResponse]
      .name("getAll")
      .description("getAll")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[Person, PersonResponse]
      .name("getById")
      .description("getById")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[Person, PersonResponse]
      .name("create")
      .description("create")
      .post
      .in(tag)
      .in(jsonBody[PersonForm.Raw])

  val updateEndpoint =
    jsonEndpoint[Person, PersonResponse]
      .name("update")
      .description("update")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[PersonForm.Raw])

  val deleteEndpoint =
    jsonEndpoint[Nothing, EmptyResponse]
      .name("delete")
      .description("delete")
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

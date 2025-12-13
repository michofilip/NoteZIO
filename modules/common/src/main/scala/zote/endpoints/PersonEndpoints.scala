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
      .description("Returns a list of all persons. [GET] /api/v1/persons")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[Person, PersonResponse]
      .description("Returns details of a person by its identifier. [GET] /api/v1/persons/{id}")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[Person, PersonResponse]
      .description("Creates a new person and returns the created object. [POST] /api/v1/persons")
      .post
      .in(tag)
      .in(jsonBody[PersonForm.Raw])

  val updateEndpoint =
    jsonEndpoint[Person, PersonResponse]
      .description("Updates a person by its identifier and returns the updated object. [PUT] /api/v1/persons/{id}")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[PersonForm.Raw])

  val deleteEndpoint =
    jsonEndpoint[Nothing, EmptyResponse]
      .description("Deletes a person by its identifier. [DELETE] /api/v1/persons/{id}")
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

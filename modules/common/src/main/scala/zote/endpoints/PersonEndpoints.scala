package zote.endpoints

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.{AnyEndpoint, path}
import zio.*
import zote.dto.Person
import zote.dto.form.PersonForm

trait PersonEndpoints extends Endpoints {
  val getAllEndpoint = baseEndpoint
    .tag("persons")
    .name("getAll")
    .description("getAll")
    .get
    .in("persons")
    .out(jsonBody[List[Person]])

  val getByIdEndpoint = baseEndpoint
    .tag("persons")
    .name("getById")
    .description("getById")
    .get
    .in("persons" / path[Long]("id"))
    .out(jsonBody[Person])

  val createEndpoint = baseEndpoint
    .tag("persons")
    .name("create")
    .description("create")
    .post
    .in("persons")
    .in(jsonBody[PersonForm])
    .out(jsonBody[Person])

  val updateEndpoint = baseEndpoint
    .tag("persons")
    .name("update")
    .description("update")
    .put
    .in("persons" / path[Long]("id"))
    .in(jsonBody[PersonForm])
    .out(jsonBody[Person])

  val deleteEndpoint = baseEndpoint
    .tag("persons")
    .name("delete")
    .description("delete")
    .delete
    .in("persons" / path[Long]("id"))
    .in(query[Option[Boolean]]("force"))
    .out(emptyOutput)

  final override val endpoints: List[AnyEndpoint] = List(
    getAllEndpoint,
    getByIdEndpoint,
    createEndpoint,
    updateEndpoint,
    deleteEndpoint
  )
}

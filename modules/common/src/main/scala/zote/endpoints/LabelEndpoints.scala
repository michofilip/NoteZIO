package zote.endpoints

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.ztapir.*
import sttp.tapir.{AnyEndpoint, path}
import zio.*
import zote.dto.Label
import zote.dto.form.LabelForm

trait LabelEndpoints extends Endpoints {

  val getAllEndpoint = baseEndpoint
    .tag("label")
    .name("getAll")
    .description("getAll")
    .get
    .in("label")
    .out(jsonBody[List[Label]])

  val getByIdEndpoint = baseEndpoint
    .tag("label")
    .name("getById")
    .description("getById")
    .get
    .in("label" / path[Long]("id"))
    .out(jsonBody[Label])

  val createEndpoint = baseEndpoint
    .tag("label")
    .name("create")
    .description("create")
    .post
    .in("label")
    .in(jsonBody[LabelForm])
    .out(jsonBody[Label])

  val updateEndpoint = baseEndpoint
    .tag("label")
    .name("update")
    .description("update")
    .put
    .in("label" / path[Long]("id"))
    .in(jsonBody[LabelForm])
    .out(jsonBody[Label])

  val deleteEndpoint = baseEndpoint
    .tag("label")
    .name("delete")
    .description("delete")
    .delete
    .in("label" / path[Long]("id"))
    .out(emptyOutput)

  final override val endpoints: List[AnyEndpoint] = List(
    getAllEndpoint,
    getByIdEndpoint,
    createEndpoint,
    updateEndpoint,
    deleteEndpoint
  )
}

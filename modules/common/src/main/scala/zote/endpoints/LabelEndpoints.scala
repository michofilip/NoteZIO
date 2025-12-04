package zote.endpoints

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import zio.*
import zote.dto.Label
import zote.dto.form.LabelForm

trait LabelEndpoints extends Endpoints {

  val getAllEndpoint = baseEndpoint
    .tag("labels")
    .name("getAll")
    .description("getAll")
    .get
    .in("labels")
    .out(jsonBody[List[Label]])

  val getByIdEndpoint = baseEndpoint
    .tag("labels")
    .name("getById")
    .description("getById")
    .get
    .in("label" / path[Long]("id"))
    .out(jsonBody[Label])

  val createEndpoint = baseEndpoint
    .tag("labels")
    .name("create")
    .description("create")
    .post
    .in("labels")
    .in(jsonBody[LabelForm.Raw])
    .out(jsonBody[Label])

  val updateEndpoint = baseEndpoint
    .tag("labels")
    .name("update")
    .description("update")
    .put
    .in("labels" / path[Long]("id"))
    .in(jsonBody[LabelForm.Raw])
    .out(jsonBody[Label])

  val deleteEndpoint = baseEndpoint
    .tag("labels")
    .name("delete")
    .description("delete")
    .delete
    .in("labels" / path[Long]("id"))
    .out(emptyOutput)

  final override val endpoints: List[AnyEndpoint] = List(
    getAllEndpoint,
    getByIdEndpoint,
    createEndpoint,
    updateEndpoint,
    deleteEndpoint,
  )
}

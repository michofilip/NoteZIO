package zote.endpoints

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import zote.dto.Label
import zote.dto.form.LabelForm
import zote.dto.response.{EmptyResponse, LabelResponse, LabelsResponse}

trait LabelEndpoints extends Endpoints {
  override protected val tag: String = "labels"

  val getAllEndpoint =
    jsonEndpoint[List[Label], LabelsResponse]
      .description("getAll")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[Label, LabelResponse]
      .description("getById")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[Label, LabelResponse]
      .description("create")
      .post
      .in(tag)
      .in(jsonBody[LabelForm.Raw])

  val updateEndpoint =
    jsonEndpoint[Label, LabelResponse]
      .description("update")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[LabelForm.Raw])

  val deleteEndpoint =
    jsonEndpoint[Nothing, EmptyResponse]
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

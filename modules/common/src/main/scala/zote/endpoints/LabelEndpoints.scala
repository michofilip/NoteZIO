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
      .description("Returns a list of all labels. [GET] /api/v1/labels")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[Label, LabelResponse]
      .description("Returns details of a label by its identifier. [GET] /api/v1/labels/{id}")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[Label, LabelResponse]
      .description("Creates a new label and returns the created object. [POST] /api/v1/labels")
      .post
      .in(tag)
      .in(jsonBody[LabelForm.Raw])

  val updateEndpoint =
    jsonEndpoint[Label, LabelResponse]
      .description("Updates a label by its identifier and returns the updated object. [PUT] /api/v1/labels/{id}")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[LabelForm.Raw])

  val deleteEndpoint =
    jsonEndpoint[Nothing, EmptyResponse]
      .description("Deletes a label by its identifier. [DELETE] /api/v1/labels/{id}")
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

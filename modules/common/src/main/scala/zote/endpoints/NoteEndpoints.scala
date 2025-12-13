package zote.endpoints

import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import zote.dto.form.NoteForm
import zote.dto.response.{EmptyResponse, NoteResponse, NotesResponse}
import zote.dto.{Note, NoteHeader}

trait NoteEndpoints extends Endpoints {
  override protected val tag: String = "notes"

  val getAllEndpoint =
    jsonEndpoint[List[NoteHeader], NotesResponse]
      .description("getAll")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[Note, NoteResponse]
      .description("getById")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[Note, NoteResponse]
      .description("create")
      .post
      .in(tag)
      .in(jsonBody[NoteForm.Raw])

  val updateEndpoint =
    jsonEndpoint[Note, NoteResponse]
      .description("update")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[NoteForm.Raw])

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

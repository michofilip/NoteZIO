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
      .description("Returns a list of all notes (headers). [GET] /api/v1/notes")
      .get
      .in(tag)

  val getByIdEndpoint =
    jsonEndpoint[Note, NoteResponse]
      .description("Returns details of a note by its identifier. [GET] /api/v1/notes/{id}")
      .get
      .in(tag / path[Long]("id"))

  val createEndpoint =
    jsonEndpoint[Note, NoteResponse]
      .description("Creates a new note and returns the created object. [POST] /api/v1/notes")
      .post
      .in(tag)
      .in(jsonBody[NoteForm.Raw])

  val updateEndpoint =
    jsonEndpoint[Note, NoteResponse]
      .description("Updates a note by its identifier and returns the updated object. [PUT] /api/v1/notes/{id}")
      .put
      .in(tag / path[Long]("id"))
      .in(jsonBody[NoteForm.Raw])

  val deleteEndpoint =
    jsonEndpoint[Nothing, EmptyResponse]
      .description("Deletes a note by its identifier. [DELETE] /api/v1/notes/{id}")
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

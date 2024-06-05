package zote.endpoints

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.{AnyEndpoint, path}
import sttp.tapir.ztapir.*
import zio.*
import zote.dto.form.NoteForm
import zote.dto.{Note, NoteHeader}

trait NoteEndpoints extends Endpoints {
  val getAllEndpoint = baseEndpoint
    .tag("notes")
    .name("getAll")
    .description("getAll")
    .get
    .in("notes")
    .out(jsonBody[List[NoteHeader]])

  val getByIdEndpoint = baseEndpoint
    .tag("notes")
    .name("getById")
    .description("getById")
    .get
    .in("notes" / path[Long]("id"))
    .out(jsonBody[Note])

  val createEndpoint = baseEndpoint
    .tag("notes")
    .name("create")
    .description("create")
    .post
    .in("notes")
    .in(jsonBody[NoteForm])
    .out(jsonBody[Note])

  val updateEndpoint = baseEndpoint
    .tag("notes")
    .name("update")
    .description("update")
    .put
    .in("notes" / path[Long]("id"))
    .in(jsonBody[NoteForm])
    .out(jsonBody[Note])

  val deleteEndpoint = baseEndpoint
    .tag("notes")
    .name("delete")
    .description("delete")
    .delete
    .in("notes" / path[Long]("id"))
    .out(emptyOutput)

  final override val endpoints: List[AnyEndpoint] = List(
    getAllEndpoint,
    getByIdEndpoint,
    createEndpoint,
    updateEndpoint,
    deleteEndpoint
  )
}

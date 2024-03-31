package zote.controllers

import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import sttp.tapir.{AnyEndpoint, path}
import zio.*
import zote.dto.{Note, NoteForm}
import zote.services.NoteService

case class NoteController(
    private val noteService: NoteService
) extends Controller {
    override protected val tag: String = "notes"

    private val getAllEndpoint = baseEndpoint
        .name("getAll")
        .description("getAll")
        .get
        .in("notes")
        .out(jsonBody[Seq[Note]])

    private val getByIdEndpoint = baseEndpoint
        .name("getById")
        .description("getById")
        .get
        .in("notes" / path[Long]("id"))
        .out(jsonBody[Note])

    private val createEndpoint = baseEndpoint
        .name("create")
        .description("create")
        .post
        .in("notes")
        .in(jsonBody[NoteForm])
        .out(jsonBody[Note])

    private val updateEndpoint = baseEndpoint
        .name("update")
        .description("update")
        .put
        .in("notes" / path[Long]("id"))
        .in(jsonBody[NoteForm])
        .out(jsonBody[Note])

    private val deleteEndpoint = baseEndpoint
        .name("delete")
        .description("delete")
        .delete
        .in("notes" / path[Long]("id"))
        .out(emptyOutput)

    private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
        noteService.getAll
    }

    private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
        noteService.getById(id)
    }

    private val create = createEndpoint.zServerLogic[Any] { noteForm =>
        noteService.create(noteForm)
    }

    private val update = updateEndpoint.zServerLogic[Any] { case (id, noteForm) =>
        noteService.update(id, noteForm)
    }

    private val delete = deleteEndpoint.zServerLogic[Any] { id =>
        noteService.delete(id)
    }

    override val endpoints: List[AnyEndpoint] = List(
        getAllEndpoint,
        getByIdEndpoint,
        createEndpoint,
        updateEndpoint,
        deleteEndpoint
    )
    override val routes: List[ServerEndpoint[Any, Task]] = List(
        getAll,
        getById,
        create,
        update,
        delete
    )
}

object NoteController {
    lazy val layer = ZLayer.derive[NoteController]
}
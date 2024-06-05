package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.dto.form.NoteForm
import zote.endpoints.NoteEndpoints
import zote.services.NoteService

case class NoteController(
    private val noteService: NoteService
) extends Controller
    with NoteEndpoints {
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

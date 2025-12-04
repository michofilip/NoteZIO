package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.Ids.NoteId
import zote.endpoints.NoteEndpoints
import zote.services.NoteService
import zote.services.validation.NoteValidationService

case class NoteController(
    private val noteService: NoteService,
    private val noteValidationService: NoteValidationService,
) extends Controller
    with NoteEndpoints {

  private val getAll = getAllEndpoint.zServerLogic[Any] { _ =>
    noteService.getAll
  }

  private val getById = getByIdEndpoint.zServerLogic[Any] { id =>
    noteService.getById(NoteId(id))
  }

  private val create = createEndpoint.zServerLogic[Any] { noteForm =>
    for {
      noteForm <- noteValidationService.validate(noteForm)
      note     <- noteService.create(noteForm)
    } yield note
  }

  private val update = updateEndpoint.zServerLogic[Any] { case (id, noteForm) =>
    for {
      noteForm <- noteValidationService.validate(noteForm)
      note     <- noteService.update(NoteId(id), noteForm)
    } yield note

  }

  private val delete = deleteEndpoint.zServerLogic[Any] { id =>
    noteService.delete(NoteId(id))
  }

  override val routes: List[ServerEndpoint[Any, Task]] = List(
    getAll,
    getById,
    create,
    update,
    delete,
  )
}

object NoteController {
  lazy val layer = ZLayer.derive[NoteController]
}

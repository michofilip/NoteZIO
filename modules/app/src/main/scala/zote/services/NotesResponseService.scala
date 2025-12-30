package zote.services

import com.raquo.laminar.api.L.{*, given}
import zote.dto.NoteHeader
import zote.dto.response.NotesResponse
import zote.utils.{BackendClient, DataStore, Fetch}

object NotesResponseService extends DataStore[NotesResponse] with Fetch {
  override def fetch(): Unit = BackendClient.notes.getAll(set)

  def getNoteHeaders: Signal[Option[List[NoteHeader]]] = get.map(_.flatMap(_.data))
}

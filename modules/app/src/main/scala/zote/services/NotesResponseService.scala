package zote.services

import zote.dto.response.NotesResponse
import zote.utils.BackendClient

object NotesResponseService extends ResponseService[NotesResponse] with Fetch {
  override def fetch(): Unit = BackendClient.notes.getAll(set)
}

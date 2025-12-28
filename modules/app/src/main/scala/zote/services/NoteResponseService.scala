package zote.services

import zote.Ids.NoteId
import zote.dto.response.NoteResponse
import zote.utils.BackendClient

object NoteResponseService extends ResponseService[NoteResponse] with FetchById[NoteId] {
  override def fetch(id: NoteId): Unit = BackendClient.notes.getById(id)(set)
}

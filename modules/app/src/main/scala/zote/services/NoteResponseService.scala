package zote.services

import com.raquo.laminar.api.L.{*, given}
import zote.Ids.NoteId
import zote.dto.Note
import zote.dto.response.NoteResponse
import zote.utils.{BackendClient, DataStore, FetchById}

object NoteResponseService extends DataStore[NoteResponse] with FetchById[NoteId] {
  override def fetch(id: NoteId): Unit = BackendClient.notes.getById(id)(set)

  def getNote: Signal[Option[Note]] = get.map(_.flatMap(_.data))
}

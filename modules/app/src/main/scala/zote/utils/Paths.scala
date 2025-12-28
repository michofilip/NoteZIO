package zote.utils

import zote.Ids.{LabelId, NoteId, UserId}

object Paths {
  def notes              = "/notes"
  def note(id: NoteId)   = s"/notes/$id"
  def users              = "/users"
  def user(id: UserId)   = s"/users/$id"
  def labels             = "/labels"
  def label(id: LabelId) = s"/labels/$id"
}

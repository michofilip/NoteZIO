package zote.utils

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import zote.Ids.{LabelId, NoteId, UserId}

object Paths {

  private val pathname: Var[String] = Var(dom.window.location.pathname)
  dom.window.addEventListener("popstate", _ => pathname.set(dom.window.location.pathname))

  def contains(path: String): Signal[Boolean] = pathname.signal.map(_.contains(path))

  private val notesStr  = "notes"
  private val usersStr  = "users"
  private val labelsStr = "labels"

  def notes              = s"/$notesStr"
  def note(id: NoteId)   = s"/$notesStr/$id"
  def users              = s"/$usersStr"
  def user(id: UserId)   = s"/$usersStr/$id"
  def labels             = s"/$labelsStr"
  def label(id: LabelId) = s"/$labelsStr/$id"

}

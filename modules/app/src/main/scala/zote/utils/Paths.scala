package zote.utils

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import zote.Ids.{LabelId, NoteId, UserId}

object Paths {

  private val pathname: Var[String] = Var(dom.window.location.pathname)
  dom.window.addEventListener("popstate", _ => pathname.set(dom.window.location.pathname))

  def contains(path: String): Signal[Boolean] = pathname.signal.map(_.contains(path))

  val notesPrefix  = "notes"
  val usersPrefix  = "users"
  val labelsPrefix = "labels"

  def notes              = s"/$notesPrefix"
  def note(id: NoteId)   = s"/$notesPrefix/$id"
  def users              = s"/$usersPrefix"
  def user(id: UserId)   = s"/$usersPrefix/$id"
  def labels             = s"/$labelsPrefix"
  def label(id: LabelId) = s"/$labelsPrefix/$id"

}

package zote.controllers

import sttp.model.StatusCode
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.exceptions.*

trait Controller {
  val routes: List[ServerEndpoint[Any, Task]]
}

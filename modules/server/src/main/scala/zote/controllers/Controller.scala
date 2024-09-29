package zote.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*

trait Controller {
  val routes: List[ServerEndpoint[Any, Task]]
}

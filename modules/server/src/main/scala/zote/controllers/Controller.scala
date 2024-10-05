package zote.controllers

import sttp.tapir.server.ServerEndpoint
import zio.*
import zote.endpoints.Endpoints

trait Controller { this: Endpoints =>
  val routes: List[ServerEndpoint[Any, Task]]
}

package zote.controllers

import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*
import zote.endpoints.HealthEndpoints

case class HealthController() extends Controller with HealthEndpoints {
  private val health =
    healthEndpoint.zServerLogic[Any](_ => ZIO.succeed("All good!"))

  override val routes: List[ServerEndpoint[Any, Task]] = List(health)
}

object HealthController {
  lazy val layer = ZLayer.derive[HealthController]
}

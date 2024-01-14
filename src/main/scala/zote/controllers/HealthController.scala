package zote.controllers

import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.ztapir.*
import zio.*

case class HealthController() extends Controller {
    private val healthEndpoint = endpoint
        .name("health")
        .description("health")
        .get
        .in("health")
        .out(stringBody)

    private val health = healthEndpoint.zServerLogic[Any](_ => ZIO.succeed("All good!"))

    override val endpoints: List[AnyEndpoint] = List(healthEndpoint)
    override val routes: List[ServerEndpoint[Any, Task]] = List(health)
}

object HealthController {
    lazy val layer = ZLayer.derive[HealthController]
}

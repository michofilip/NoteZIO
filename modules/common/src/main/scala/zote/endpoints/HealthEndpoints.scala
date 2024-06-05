package zote.endpoints

import sttp.tapir.AnyEndpoint
import sttp.tapir.ztapir.*

trait HealthEndpoints extends Endpoints {
  val healthEndpoint = baseEndpoint
    .tag("health")
    .name("health")
    .description("health")
    .get
    .in("health")
    .out(stringBody)

  final override val endpoints: List[AnyEndpoint] = List(healthEndpoint)
}

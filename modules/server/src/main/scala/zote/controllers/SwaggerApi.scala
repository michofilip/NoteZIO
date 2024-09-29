package zote.controllers

import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.*

object SwaggerApi {
  def routesZIO = {
    for {
      endpoints <- HttpApi.endpointsZIO
      routes <- ZIO.succeed(
        SwaggerInterpreter()
          .fromEndpoints[Task](endpoints, "Zote", "0.1.0-SNAPSHOT")
      )
    } yield routes
  }
}

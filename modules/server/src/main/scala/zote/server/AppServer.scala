package zote.server

import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.*
import zio.http.Server
import zote.controllers.{Controller, LabelController, NoteController, UserController}
import zote.endpoints.Endpoints

case class AppServer(
    private val noteController: NoteController,
    private val userController: UserController,
    private val labelController: LabelController,
) {
  private val controllers: List[Controller & Endpoints] = List(
    noteController,
    userController,
    labelController,
  )

  def install: ZIO[Server, Nothing, Int] = {
    val endpoints     = controllers.flatMap(_.endpoints)
    val routes        = controllers.flatMap(_.routes)
    val swaggerRoutes = SwaggerInterpreter().fromEndpoints[Task](endpoints, "Zote", "0.1.0-SNAPSHOT")
    val serverOptions = ZioHttpServerOptions.default[Any].appendInterceptor(CORSInterceptor.default)

    Server.install(ZioHttpInterpreter(serverOptions).toHttp(routes ++ swaggerRoutes))
  }
}

object AppServer {
  lazy val layer = ZLayer.derive[AppServer]
}

package zote.controllers

import zio.*

object HttpApi {

    def routesZIO = controllers.map(_.flatMap(_.routes))

    def endpointsZIO = controllers.map(_.flatMap(_.endpoints))

    private def controllers = for {
        healthController <- ZIO.service[HealthController]
        noteController <- ZIO.service[NoteController]
        userController <- ZIO.service[UserController]
    } yield List(
        healthController,
        noteController,
        userController
    )
}

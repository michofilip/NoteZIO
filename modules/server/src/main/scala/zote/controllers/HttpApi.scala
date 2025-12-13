package zote.controllers

import zio.*

object HttpApi {

  def routesZIO = controllers.map(_.flatMap(_.routes))

  def endpointsZIO = controllers.map(_.flatMap(_.endpoints))

  private def controllers = for {
    noteController   <- ZIO.service[NoteController]
    personController <- ZIO.service[PersonController]
    labelController  <- ZIO.service[LabelController]
  } yield List(
    noteController,
    personController,
    labelController,
  )
}

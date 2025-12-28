package zote.config

import sttp.client4.UriContext
import sttp.model.Uri
import zio.ZLayer

case class BackendClientConfig(
    baseUri: Uri,
)

object BackendClientConfig {
  lazy val layer = ZLayer.succeed(BackendClientConfig(uri"http://localhost:8080"))
}

package zote.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

case class FlywayConfig(locations: String)

object FlywayConfig {
    lazy val layer = ZLayer.fromZIO {
        ConfigProvider.fromResourcePath().nested("flyway").load(deriveConfig[FlywayConfig])
    }
}

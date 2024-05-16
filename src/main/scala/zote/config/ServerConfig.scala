package zote.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*
import zio.http.Server

case class ServerConfig(port: Int)

object ServerConfig {
  lazy val layer = ZLayer.fromZIO {
    ConfigProvider.fromResourcePath().nested("network").load(deriveConfig[ServerConfig])
  }.flatMap(conf => Server.defaultWithPort(conf.get.port))
}

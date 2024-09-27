package zote.config

import zio.Runtime
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object SLF4JConfig {
  lazy val layer = SLF4J.slf4j(LogFormat.colored)
//  lazy val layer = Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)
}

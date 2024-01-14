package zote.services

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateErrorResult
import zio.*
import zote.config.FlywayConfig
import zote.exceptions.DbMigrationFailedException

import javax.sql.DataSource

trait FlywayService {
    protected def run: Task[Unit]
}

object FlywayService {
    lazy val layer = ZLayer.derive[FlywayServiceImpl]
    def run: ZIO[FlywayService, Throwable, Unit] = ZIO.serviceWithZIO[FlywayService](_.run)
}

case class FlywayServiceImpl(
    private val flywayConfig: FlywayConfig,
    private val dataSource: DataSource
) extends FlywayService {
    override protected def run: Task[Unit] = ZIO.attemptUnsafe { _ =>
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations(flywayConfig.locations)
                .load()

            flyway.migrate()
        }
        .flatMap {
            case r: MigrateErrorResult => ZIO.fail(DbMigrationFailedException(r.error.message))
            case _ => ZIO.unit
        }
        .onError(cause => ZIO.logErrorCause("Database migration has failed", cause))
}

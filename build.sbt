ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.2"

val zioVersion = "2.1.2"
val zioJsonVersion = "0.6.2"
val zioConfigVersion = "4.0.2"
val zioLoggingVersion = "2.3.0"
val zioPreludeVersion = "1.0.0-RC27"
val slf4jVersion = "2.0.13"
val quillVersion = "4.8.4"
val postgresqlVersion = "42.7.3"
val flywayVersion = "10.14.0"
val quicklensVersion = "1.9.7"
val tapirVersion = "1.10.8"
val sttpVersion = "3.9.7"
val testcontainersPostgresqlVersion = "0.10.0"

lazy val common = (project in file("modules/common"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"    % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "io.getquill" %% "quill-jdbc-zio" % quillVersion,
      "dev.zio"               %% "zio-prelude"                       % zioPreludeVersion,
    )
  )

lazy val server = (project in file("modules/server"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,

      "dev.zio" %% "zio-json" % zioJsonVersion,

      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
      "dev.zio" %% "zio-config-refined" % zioConfigVersion,

      "dev.zio" %% "zio-logging" % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "org.slf4j" % "slf4j-simple" % slf4jVersion,

      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"  % tapirVersion % Test,
      "com.softwaremill.sttp.client3" %% "zio"               % sttpVersion,

      "io.getquill" %% "quill-jdbc-zio" % quillVersion,
      "org.postgresql" % "postgresql" % postgresqlVersion,
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion,
      "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % testcontainersPostgresqlVersion,
      "io.github.scottweaver" %% "zio-2-0-db-migration-aspect" % testcontainersPostgresqlVersion,

      "com.softwaremill.quicklens" %% "quicklens" % quicklensVersion
    )
  )
//  .dependsOn(common.jvm)
  .dependsOn(common)

lazy val root = (project in file("."))
  .settings(
    name := "NoteZIO",
  )
  .aggregate(server)
  .dependsOn(server)
//  .aggregate(server, app)
//  .dependsOn(server, app)

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

val zioVersion = "2.0.20"
val zioHTTPVersion = "3.0.0-RC4"
val zioJsonVersion = "0.6.2"
val zioConfigVersion = "4.0.0-RC16"
val zioLoggingVersion = "2.1.16"
val slf4jVersion = "2.0.9"
val quillVersion = "4.8.0"
val postgresqlVersion = "42.7.1"
val flywayVersion = "10.3.0"
val quicklensVersion = "1.9.6"
val tapirVersion = "1.9.6"

lazy val root = (project in file("."))
    .settings(
        name := "NoteZIO",
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

            "io.getquill" %% "quill-jdbc-zio" % quillVersion,
            "org.postgresql" % "postgresql" % postgresqlVersion,
            "org.flywaydb" % "flyway-core" % flywayVersion,
            "org.flywaydb" % "flyway-database-postgresql" % flywayVersion,

            "com.softwaremill.quicklens" %% "quicklens" % quicklensVersion,
        )
    )

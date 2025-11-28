ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.4"

val zioVersion        = "2.1.23"
val zioJsonVersion    = "0.7.45"
val zioConfigVersion  = "4.0.6"
val zioLoggingVersion = "2.5.2"
val zioPreludeVersion = "1.0.0-RC42"
val slf4jVersion      = "2.0.17"
val quillVersion      = "4.8.6"
//val postgresqlVersion = "42.7.4"
val h2Version        = "2.4.240"
val flywayVersion    = "11.18.0"
val quicklensVersion = "1.9.12"
val tapirVersion     = "1.12.5"
val sttpVersion      = "3.11.0"
//val sttpVersion = "4.0.0-M18"
val frontrouteVersion = "0.19.1"

lazy val common = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/common"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"                     %% "zio"                   % zioVersion,
      "dev.zio"                     %% "zio-test"              % zioVersion % Test,
      "dev.zio"                     %% "zio-test-sbt"          % zioVersion % Test,
      "dev.zio"                     %% "zio-json"              % zioJsonVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio"        % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
      "dev.zio"                     %% "zio-prelude"           % zioPreludeVersion,
      "com.softwaremill.quicklens"  %% "quicklens"             % quicklensVersion,
    ),
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.5.0", // implementations of java.time classes for Scala.JS,
    ),
  )

lazy val server = (project in file("modules/server"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
// JSON
      "dev.zio" %% "zio-json" % zioJsonVersion,
// Config
      "dev.zio" %% "zio-config"          % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia" % zioConfigVersion,
      "dev.zio" %% "zio-config-refined"  % zioConfigVersion,
// Logging
      "dev.zio"  %% "zio-logging"       % zioLoggingVersion,
      "dev.zio"  %% "zio-logging-slf4j" % zioLoggingVersion,
      "org.slf4j" % "slf4j-simple"      % slf4jVersion,
// Http
      "com.softwaremill.sttp.tapir"   %% "tapir-zio-http-server"   % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-zio"          % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"  % tapirVersion % Test,
      "com.softwaremill.sttp.client3" %% "zio"                     % sttpVersion,
//      "com.softwaremill.sttp.client4" %% "zio" % sttpVersion,
// DB
      "io.getquill" %% "quill-jdbc-zio" % quillVersion,
//      "org.postgresql" % "postgresql" % postgresqlVersion,
      "com.h2database" % "h2"                         % h2Version,
      "org.flywaydb"   % "flyway-core"                % flywayVersion,
      "org.flywaydb"   % "flyway-database-postgresql" % flywayVersion,
// Other
      "com.softwaremill.quicklens" %% "quicklens" % quicklensVersion,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )
  .dependsOn(common.jvm)

lazy val app = (project in file("modules/app"))
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %%% "tapir-sttp-client" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %%% "tapir-json-zio"    % tapirVersion,
      "com.softwaremill.sttp.client3" %%% "zio"               % sttpVersion,
//      "com.softwaremill.sttp.client4" %%% "zio" % sttpVersion,
      "dev.zio"       %%% "zio-json"   % zioJsonVersion,
      "dev.frontroute" %%% "frontroute" % frontrouteVersion, // Brings in Laminar 17
    ),
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    semanticdbEnabled               := true,
    autoAPIMappings                 := true,
    scalaJSUseMainModuleInitializer := true,
    Compile / mainClass             := Some("zote.App"),
  )
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(common.js)

lazy val root = (project in file("."))
  .settings(
    name := "NoteZIO",
  )
  .aggregate(server, app)
  .dependsOn(server, app)

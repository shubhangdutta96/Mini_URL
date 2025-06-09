import sbt.enablePlugins
import sbt.Keys.*
import sbt.*
import sbtassembly.AssemblyPlugin.autoImport._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

libraryDependencies ++= Seq(
  // Akka HTTP & Akka Streams
  "com.typesafe.akka" %% "akka-http"                % "10.2.10",
  "com.typesafe.akka" %% "akka-stream"              % "2.6.20",
  "com.typesafe.akka" %% "akka-actor-typed"         % "2.6.20",
  "com.typesafe.akka" %% "akka-http-spray-json"     % "10.2.10",
  "net.liftweb" %% "lift-json"                      % "3.4.0",

  // PureConfig
  "com.github.pureconfig" %% "pureconfig"           % "0.17.4",

  // Slick (Functional Relational Mapping)
  "com.typesafe.slick" %% "slick"                   % "3.4.1",
  "com.typesafe.slick" %% "slick-hikaricp"          % "3.4.1",

  // PostgreSQL JDBC Driver
  "org.postgresql" % "postgresql"                   % "42.7.1",

  // Logging
  "ch.qos.logback" % "logback-classic"              % "1.4.11",
  "org.slf4j"      % "slf4j-api"                    % "2.0.9"

)

Compile / mainClass := Some("Main")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}
enablePlugins(DockerPlugin)
enablePlugins(JavaAppPackaging)

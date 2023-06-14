ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "es-01"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0",

  "com.typesafe.akka" %% "akka-slf4j" % "2.8.0",
  "ch.qos.logback" % "logback-classic" % "1.4.7" % Runtime
)

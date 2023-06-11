ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.3"

lazy val root = (project in file("."))
  .settings(
    name := "es-01"
  )

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0"

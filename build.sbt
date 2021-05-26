name := """todo-list"""
organization := "com.github.ntngel1"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice

// Cats
libraryDependencies += "org.typelevel" %% "cats-core" % "2.6.1"

// Play Framework
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"

// ReactiveMongo
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "1.0.4-play28"
libraryDependencies += "org.reactivemongo" %% "reactivemongo-bson" % "0.20.12"

// Sentry
libraryDependencies += "io.sentry" % "sentry" % "4.3.0"
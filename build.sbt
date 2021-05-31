name := """todo-list"""
organization := "com.github.ntngel1"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice

// Cats
libraryDependencies += "org.typelevel" %% "cats-core" % "2.6.1"

// Play Framework
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.2"

// ReactiveMongo
// TODO: I'm really confused about versioning in ReactiveMongo. I spent like 6 hours trying to resolve working
//       combination of versions and libraries. I had several issues like "BSONObjectID should be JsValueWrapper"
//       (kinda like that) or missing implicit for conversion JsObject to BSONDocument (no Writer, Reader). I don't know
//       why but if I change ReactiveMongo version from "0.20.13-play28" to "1.0.4-play28", it will not work.
//       Also I don't actually understand why should I import BSONObjectID from package "reactivemongo.bson" and not from
//       "api.reactivemongo.bson" (it will not work else)
libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.20.13-play28"
libraryDependencies += "org.reactivemongo" %% "reactivemongo-play-json-compat" % "1.0.4-play28"

// Sentry
//libraryDependencies += "io.sentry" % "sentry" % "4.3.0"
libraryDependencies +=   "io.sentry" % "sentry-logback" % "1.7.16"

// Testing
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.mockito" % "mockito-core" % "3.10.0" % Test
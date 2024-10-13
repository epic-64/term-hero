ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.5.0"

libraryDependencies ++= Seq(
    "com.googlecode.lanterna" % "lanterna" % "3.1.2",
    "com.lihaoyi" %% "upickle" % "4.0.2",
    "org.scalatest" %% "scalatest" % "3.2.18" % "test",
)

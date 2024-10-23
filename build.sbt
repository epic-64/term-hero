ThisBuild / version      := "0.0.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.5.0"
ThisBuild / javacOptions ++= Seq("--release", "17")

libraryDependencies ++= Seq(
  "com.googlecode.lanterna" % "lanterna"     % "3.1.2",
  "com.lihaoyi"            %% "upickle"      % "4.0.2",
  "org.scalatest"          %% "scalatest"    % "3.2.18"   % Test,
  "org.scalamock"          %% "scalamock"    % "6.0.0"    % Test,
  "org.scalatestplus"      %% "mockito-5-12" % "3.2.19.0" % Test
)
